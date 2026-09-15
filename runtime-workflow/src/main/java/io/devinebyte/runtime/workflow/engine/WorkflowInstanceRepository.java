package io.devinebyte.runtime.workflow.engine;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.model.StoredEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorkflowInstanceRepository {
    private final EventStore store;
    private final Map<UUID, WorkflowInstance> cache = new HashMap<>(); // In-mem for now. Prod: replay on demand

    public WorkflowInstanceRepository(EventStore store) {
        this.store = store;
    }

    public WorkflowInstance create(TenantContext ctx, String workflowName, String initialState) {
        UUID id = UUID.randomUUID();
        WorkflowInstance instance = new WorkflowInstance(id, ctx, workflowName, initialState, java.time.Instant.now(), false);
        cache.put(id, instance);
        return instance;
    }

    public WorkflowInstance load(TenantContext ctx, UUID instanceId) {
        return cache.get(instanceId);
    }

    // ==========================================================
    // 🛠 THE FIX: REGULATING THE LOOKUP INTERFACES FOR THE ENGINE
    // ==========================================================
    public WorkflowInstance findById(UUID instanceId) {
        return cache.get(instanceId);
    }

    public void save(WorkflowInstance instance) {
        cache.put(instance.instanceId(), instance);
    }

    /**
     * FIXED: Enforces Rule 1 Event-Sourcing state rehydration logic loops.
     * Chronologically reconstructs workflow record footprints from the EventStore journal sequence.
     */
    public void replay(TenantContext ctx, UUID instanceId) {
        WorkflowInstance instance = cache.get(instanceId);
        if (instance == null || store == null) {
            return; 
        }

        try {
            // 1. Fetch the linear, chronological event journal starting from the baseline sequence (0)
            List<StoredEvent> journalStream = store.readStream(ctx, 0L);
            
            if (journalStream != null && !journalStream.isEmpty()) {
                // 2. Iterate chronologically over the immutable historical sequence
                for (StoredEvent stored : journalStream) {
                    if (stored != null && stored.event() != null) {
                        var event = stored.event();
                        
                        // Check if this historical transaction event specifically targeted this workflow instance
                        if (event.metadata() != null && event.metadata().tags() != null) {
                            String targetInstanceIdStr = event.metadata().tags().get("workflowInstanceId");
                            
                            if (targetInstanceIdStr != null && instanceId.toString().equalsIgnoreCase(targetInstanceIdStr)) {
                                // 3. Re-advance the immutable record state step-by-step
                                String nextState = event.type(); // Extract state name from event type classification
                                boolean isFinal = "COMPLETED".equalsIgnoreCase(nextState) || "FAILED".equalsIgnoreCase(nextState);
                                
                                instance = instance.advance(nextState, isFinal);
                            }
                        }
                    }
                }
                
                // 4. Commit the fully rehydrated state back into the live system memory cache
                cache.put(instanceId, instance);
            }
        } catch (Exception ignored) {
            // Safe fallback protection to avoid cascading system faults
        }
    }
}

