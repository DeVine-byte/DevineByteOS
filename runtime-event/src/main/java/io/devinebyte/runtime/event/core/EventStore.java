package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.StoredEvent;
import java.util.List;
import java.util.function.Consumer;

public interface EventStore {
    StoredEvent append(TenantContext ctx, DomainEvent event);
    List<StoredEvent> readStream(TenantContext ctx, long fromSequence);
    long getLastSequence(TenantContext ctx);
    
    default void replay(TenantContext ctx, long fromSequence, Consumer<DomainEvent> handler) {
        readStream(ctx, fromSequence).forEach(s -> handler.accept(s.event()));
    }
}
