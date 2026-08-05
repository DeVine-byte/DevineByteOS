package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.EventMetadata;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import java.time.Instant;
import java.util.UUID;

public class WorkflowExecutor {
    private final EventStore store;
    private final ModuleIsolationGuard guard;

    public WorkflowExecutor(EventStore store, ModuleIsolationGuard guard) {
        this.store = store;
        this.guard = guard;
    }

    public WorkflowInstance handleEvent(TenantContext ctx, WorkflowDefinition def, WorkflowInstance instance, DomainEvent event) {
        guard.assertEnabled(ctx, "workflow", "handleEvent for " + def.name()); // FIX

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
