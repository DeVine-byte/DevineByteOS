package io.devinebyte.runtime.workflow.engine;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.model.DomainEvent;
import java.util.HashMap;
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

    public void save(WorkflowInstance instance) {
        cache.put(instance.instanceId(), instance);
    }

    public void replay(TenantContext ctx, UUID instanceId) {
        // TODO: Fold events from EventStore to rebuild state. Rule 1: Event Sourced Only
    }
}
