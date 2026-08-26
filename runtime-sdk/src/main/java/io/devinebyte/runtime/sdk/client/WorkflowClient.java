package io.devinebyte.runtime.sdk.client;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventBus;
import java.util.function.Supplier;

public record WorkflowClient(
    Supplier<TenantContext> tenantSupplier,
    EventBus eventBus
) {
    // TODO: add start(), signal()
}
