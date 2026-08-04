package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class EventBus {
    private final EventStore eventStore;
    private final EventDispatcher dispatcher;
    private final io.devinebyte.runtime.module.ModuleIsolationGuard guard;

    @Inject
    public EventBus(EventStore eventStore, EventDispatcher dispatcher, io.devinebyte.runtime.module.ModuleIsolationGuard guard) {
        this.eventStore = eventStore;
        this.dispatcher = dispatcher;
        this.guard = guard;
    }

    public void publish(TenantContext ctx, DomainEvent event) {
        guard.assertEnabled(ctx, event.metadata().sourceModule(), "publish:" + event.type());
        eventStore.append(ctx, event);
        dispatcher.dispatch(ctx, event);
    }
}
