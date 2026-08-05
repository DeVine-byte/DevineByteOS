package io.devinebyte.runtime.projection.engine;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.projection.store.ProjectionStateStore;

public record ProjectionContext(
    TenantContext tenant,
    DomainEvent triggeringEvent,
    ProjectionStateStore stateStore,
    ModuleIsolationGuard guard,
    DiagnosticCollector diagnostics
) {}
