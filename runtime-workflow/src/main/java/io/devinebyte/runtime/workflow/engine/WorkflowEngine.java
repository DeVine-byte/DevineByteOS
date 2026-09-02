package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import io.devinebyte.runtime.repository.RepositoryFactory;
import io.devinebyte.runtime.repository.EntityRepository;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class WorkflowEngine {
    private WorkflowInstanceRepository repo;
    private WorkflowExecutor executor;
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WorkflowEngine(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    public void wireDependencies(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    @SuppressWarnings("unchecked")
    public Object start(TenantContext ctx, String command, JsonNode body, String commandOrQuery) {
        System.out.println("[WORKFLOW] Starting " + commandOrQuery + ": " + command + " with " + body);
        System.out.println("[WORKFLOW DEBUG] Active registered workflows in this engine instance: " + definitions.keySet());

        WorkflowDefinition def = definitions.get(command);
        if (def == null) {
            throw new IllegalArgumentException("No workflow definition for: " + command);
        }

        Map<String, Object> input = MAPPER.convertValue(body, Map.class);
        Map<String, Object> runtimeContext = new java.util.HashMap<>(input);

        // Core Platform Hook: Intercept and process implicit compiler-driven mutations
        if (command.startsWith("Handle") && command.endsWith("POST")) {
            try {
                String tenantId = ctx.tenantId();
                String moduleId = def.moduleId(); 
                
                // Derive entity destination (e.g., HandleCustomerPOST -> Customer)
                String entityName = command
                    .replace("Handle", "")
                    .replace("POST", "")
                    .replace("GET", "")
                    .replace("PUT", "");

                System.out.println("[ENGINE PERSISTENCE] Routing context directly to generic database gateway for: " + entityName);
                
                EntityRepository entityRepo = RepositoryFactory.get(tenantId, moduleId, entityName);
                String persistentId = entityRepo.upsert(runtimeContext);
                
                // EXPLICIT DEBUG CHECKPOINT: Confirm database processing has completed
                System.out.println("[ENGINE DEBUG] Successfully wrote record to DB with ID: " + persistentId);
                
                runtimeContext.put("id", persistentId);
                runtimeContext.put("status", "SUCCESS");
                return runtimeContext;
                
            } catch (Exception ex) {
                System.err.println("[CORE CRASH] Automatic structural transaction failed: " + ex.getMessage());
                throw new RuntimeException("500: Internal Server Error - DB_UNAVAILABLE: " + ex.getMessage(), ex);
            }
        }

        // Fall back to standard long-running state tracking rules for explicit workflows
        if (this.executor == null) {
            throw new IllegalStateException("State machine executor reference is completely unassigned.");
        }
        return this.executor.start(ctx, def, input);
    }

    public boolean isSubscribedTo(String eventType) {
        return definitions.values().stream()
            .anyMatch(d -> d.findTransition(null, eventType) != null);
    }

    public WorkflowDefinition getDefinition(String workflowName) {
        return definitions.get(workflowName);
    }

    public void handleEvent(TenantContext ctx, UUID instanceId, DomainEvent event) {
        if (repo == null || executor == null) return;
        WorkflowInstance instance = repo.load(ctx, instanceId);
        if (instance == null) return;
        WorkflowDefinition def = definitions.get(instance.workflowName());
        if (def == null) return;
        WorkflowInstance newInstance = executor.handleEvent(ctx, def, instance, event);
        repo.save(newInstance);
    }

    public void register(WorkflowDefinition def) {
        definitions.put(def.name(), def);
    }
}

