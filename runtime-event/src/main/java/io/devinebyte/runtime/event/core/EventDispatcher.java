package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.handler.HandlerRegistry;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class EventDispatcher {
    private final HandlerRegistry registry;
    private final ModuleIsolationGuard guard;

    @Inject
    public EventDispatcher(HandlerRegistry registry, ModuleIsolationGuard guard) {
        this.registry = registry;
        this.guard = guard;
    }

    public void dispatch(TenantContext ctx, DomainEvent event) {
        registry.getHandlers(event.type())
            .stream()
            .filter(h -> {
                try {
                    guard.assertEnabled(ctx, h.moduleId(), "handle:" + event.type());
                    return true;
                } catch (Exception e) {
                    return false; // module disabled, skip handler
                }
            })
            .forEach(h -> h.handle(ctx, event));
    }
}
