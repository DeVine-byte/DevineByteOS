package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class SimpleEventBus {
    private final EventStore eventStore;
    private final CompositeEventDispatcher dispatcher; // CHANGED
    private final ModuleIsolationGuard guard;

    @Inject
    public SimpleEventBus(EventStore eventStore, CompositeEventDispatcher dispatcher, ModuleIsolationGuard guard) {
        this.eventStore = eventStore;
        this.dispatcher = dispatcher;
        this.guard = guard;
    }

    public void publish(TenantContext ctx, DomainEvent event) {
        guard.assertEnabled(ctx, event.metadata().sourceModule(), "publish:" + event.type());
        eventStore.append(ctx, event);
        dispatcher.dispatch(ctx, event); // now calls composite
    }
}
