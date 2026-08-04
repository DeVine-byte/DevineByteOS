package io.devinebyte.runtime.event.handler;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;

public interface EventHandler {
    String moduleId();
    String eventType();
    void handle(TenantContext ctx, DomainEvent event);
}
