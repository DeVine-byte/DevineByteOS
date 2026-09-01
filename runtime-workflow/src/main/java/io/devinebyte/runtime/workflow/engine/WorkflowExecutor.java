package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.EventMetadata;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class WorkflowExecutor {
    private final EventStore store;
    private final ModuleIsolationGuard guard;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WorkflowExecutor(EventStore store, ModuleIsolationGuard guard) {
        this.store = store;
        this.guard = guard;
    }

    // Entry point for API commands
    public Object start(TenantContext ctx, WorkflowDefinition def, Map<String, Object> input) {
        // FIX: Query the actual owning module context directly to clear [DBRT009] validation errors!
        guard.assertEnabled(ctx, def.moduleId(), "start " + def.name());

        String instanceId = UUID.randomUUID().toString();
        String initialState = def.initialState();

        WorkflowInstance instance = new WorkflowInstance(
            UUID.fromString(instanceId), ctx, def.name(), initialState, Instant.now(), false
        );

        System.out.println("[WORKFLOW] Executing " + def.name() + " in state " + initialState);

        Map<String, Object> output = new java.util.HashMap<>(input);
        output.put("workflowInstanceId", instanceId);
        output.put("workflow", def.name());
        return output;
    }

    public WorkflowInstance handleEvent(TenantContext ctx, WorkflowDefinition def, WorkflowInstance instance, DomainEvent event) {
        // FIX: Updated security guard validation reference here too
        guard.assertEnabled(ctx, def.moduleId(), "handleEvent for " + def.name());

        var transition = def.findTransition(instance.currentState(), event.type());
        if (transition == null) return instance;

        String nextState = transition.targetState();
        boolean isFinal = "END".equals(nextState) || def.statesByName().get(nextState).isFinal();
        WorkflowInstance newInstance = instance.advance(nextState, isFinal);

        ObjectNode payload = (ObjectNode) event.payload();
        payload.put("workflowInstanceId", instance.instanceId().toString());
        payload.put("fromState", instance.currentState());
        payload.put("toState", nextState);
        payload.put("action", transition.action());

        EventMetadata meta = new EventMetadata(UUID.randomUUID(), null, "workflow", Instant.now(),
            java.util.Map.of("tenantId", ctx.tenantId(), "workflow", def.name()));
        DomainEvent stateEvent = new DomainEvent("WorkflowTransitioned", "1.0", payload, meta);
        store.append(ctx, stateEvent);
        return newInstance;
    }
}

