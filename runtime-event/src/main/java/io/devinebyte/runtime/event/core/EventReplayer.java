package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public final class EventReplayer {
    private final EventStore eventStore;
    private final EventDispatcher dispatcher;

    @Inject
    public EventReplayer(EventStore eventStore, EventDispatcher dispatcher) {
        this.eventStore = eventStore;
        this.dispatcher = dispatcher;
    }

    public void replayToCurrent(TenantContext ctx) {
        eventStore.replay(ctx, 0, event -> dispatcher.dispatch(ctx, event));
    }

    public void replayFrom(TenantContext ctx, long fromSeq, Consumer<DomainEvent> projector) {
        eventStore.replay(ctx, fromSeq, projector);
    }
}
