package io.devinebyte.runtime.plugin;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.observability.StructuredLogger;
import io.devinebyte.runtime.observability.Metrics;

public record PluginContext(
    TenantContext tenant,
    EventBus eventBus,
    PluginContractRegistry contracts,
    Configuration configuration,
    StructuredLogger logger,
    Metrics metrics,
    RuntimeServices services
) {}
