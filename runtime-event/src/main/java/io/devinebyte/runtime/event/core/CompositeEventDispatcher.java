package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import java.util.List;

public final class CompositeEventDispatcher {
    private final List<EventDispatcher> delegates;

    public CompositeEventDispatcher(List<EventDispatcher> delegates) {
        this.delegates = delegates;
    }

    public void dispatch(TenantContext ctx, DomainEvent event) {
        delegates.forEach(d -> d.dispatch(ctx, event));
    }
}
