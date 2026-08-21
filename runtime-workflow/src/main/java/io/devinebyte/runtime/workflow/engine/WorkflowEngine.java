package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.JsonNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorkflowEngine {
    private final WorkflowInstanceRepository repo;
    private final WorkflowExecutor executor;
    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();

    public WorkflowEngine(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    public Object start(TenantContext ctx, String command, JsonNode body, String commandOrQuery) { // UPDATED SIG
        System.out.println("[WORKFLOW] Starting " + commandOrQuery + ": " + command + " with " + body);
        // TODO: map command -> WorkflowDefinition and call executor.start
        return Map.of("status", "started", "command", command, "type", commandOrQuery);
    }

    public boolean isSubscribedTo(String eventType) {
        return definitions.values().stream()
            .anyMatch(d -> d.findTransition(null, eventType) != null);
    }

    public WorkflowDefinition getDefinition(String workflowName) {
        return definitions.get(workflowName);
    }

    public void handleEvent(TenantContext ctx, UUID instanceId, DomainEvent event) {
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
