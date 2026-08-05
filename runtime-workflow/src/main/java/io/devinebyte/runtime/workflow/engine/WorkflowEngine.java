package io.devinebyte.runtime.workflow.engine;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class WorkflowEngine {
    private final WorkflowInstanceRepository repo;
    private final WorkflowExecutor executor;
    private Map<String, WorkflowDefinition> definitions = Map.of();
    private final Set<String> subscribedEvents = new HashSet<>();

    public WorkflowEngine(WorkflowInstanceRepository repo, WorkflowExecutor executor) {
        this.repo = repo;
        this.executor = executor;
    }

    public void register(Map<String, io.devinebyte.compiler.workflow.model.ExecutableStateMachine> machines) {
        this.definitions = machines.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> WorkflowDefinition.from(e.getValue())));

        // Build subscription list by scanning all states
        subscribedEvents.clear();
        definitions.values().forEach(def ->
            def.statesByName().values().forEach(state ->
                state.transitions().forEach(t -> subscribedEvents.add(t.triggerEvent()))
            )
        );
    }

    public boolean isSubscribedTo(String eventType) {
        return subscribedEvents.contains(eventType);
    }

    public WorkflowInstance start(TenantContext ctx, String workflowName, com.fasterxml.jackson.databind.JsonNode input) {
        WorkflowDefinition def = definitions.get(workflowName);
        if (def == null) throw new IllegalArgumentException("Workflow not found: " + workflowName);
        return repo.create(ctx, workflowName, def.initialState());
    }

    public WorkflowDefinition getDefinition(String name) { return definitions.get(name); }

    public WorkflowInstance handleEvent(TenantContext ctx, UUID instanceId, DomainEvent event) {
        WorkflowInstance instance = repo.load(ctx, instanceId);
        if (instance == null || instance.completed()) return null; // FIX: was isFinal()
        WorkflowDefinition def = definitions.get(instance.workflowName());
        WorkflowInstance updated = executor.handleEvent(ctx, def, instance, event);
        repo.save(updated);
        return updated;
    }
}
