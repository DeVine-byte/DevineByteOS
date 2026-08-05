package io.devinebyte.runtime.workflow.engine;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.workflow.WorkflowInstanceReader;
import io.devinebyte.runtime.core.workflow.WorkflowInstanceView;
import io.devinebyte.runtime.event.handler.EventHandler;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import java.util.List;

public record WorkflowEventDispatcher(WorkflowEngine engine, WorkflowInstanceReader reader)
    implements EventHandler {

    @Override
    public String eventType() {
        return "*"; // Subscribe to all. We filter with engine.isSubscribedTo()
    }

    @Override
    public String moduleId() {
        return "workflow"; // This identifies this handler to the ModuleIsolationGuard
    }

    @Override
    public void handle(TenantContext ctx, DomainEvent event) {
        if (!engine.isSubscribedTo(event.type())) return;

        List<WorkflowInstanceView> active = reader.findActiveByTenant(ctx);
        for (WorkflowInstanceView view : active) {
            WorkflowDefinition def = engine.getDefinition(view.workflowName());
            if (def != null && def.findTransition(view.currentState(), event.type()) != null) {
                engine.handleEvent(ctx, view.instanceId(), event);
            }
        }
    }
}
