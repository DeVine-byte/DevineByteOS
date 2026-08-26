package io.devinebyte.runtime.sdk.client;

import io.devinebyte.runtime.sdk.context.RuntimeContext;
import io.devinebyte.runtime.event.core.EventBus;

public record RuntimeClient(
    EventBus eventBus,
    EntityClient entities,
    WorkflowClient workflows
) {
    public RuntimeClient(EventBus eventBus) {
        this(eventBus, new EntityClient(RuntimeContext::current, eventBus), new WorkflowClient(RuntimeContext::current, eventBus));
    }
}
