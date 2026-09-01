package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import io.devinebyte.runtime.workflow.engine.WorkflowInstance;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class WorkflowEngine {
    // FIX: Removed 'final' so these can be safely updated when the tenant workspace initializes
    private WorkflowInstanceRepository repo;
    private WorkflowExecutor executor;
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WorkflowEngine(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    // FIX: Explicitly updates engine references with valid event stores and module guards
    public void wireDependencies(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    public Object start(TenantContext ctx, String command, JsonNode body, String commandOrQuery) {
        System.out.println("[WORKFLOW] Starting " + commandOrQuery + ": " + command + " with " + body);
        System.out.println("[WORKFLOW DEBUG] Active registered workflows in this engine instance: " + definitions.keySet());

        WorkflowDefinition def = definitions.get(command);
        if (def == null) {
            throw new IllegalArgumentException("No workflow definition for: " + command);
        }

        Map<String, Object> input = MAPPER.convertValue(body, Map.class);
        return executor.start(ctx, def, input);
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

