package io.devinebyte.runtime.event.core;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;

public class NoOpEventDispatcher {
    public void dispatch(TenantContext ctx, DomainEvent event) {
        // North Star 6: append only. No dispatch yet.
    }
}
