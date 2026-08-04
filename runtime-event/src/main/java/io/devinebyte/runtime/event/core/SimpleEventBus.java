package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.module.ModuleIsolationGuard;

public final class SimpleEventBus {
    private final EventStore eventStore;
    private final NoOpEventDispatcher dispatcher; // CHANGED TYPE
    private final ModuleIsolationGuard guard;

    public SimpleEventBus(EventStore eventStore, NoOpEventDispatcher dispatcher, ModuleIsolationGuard guard) { // CHANGED TYPE
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
