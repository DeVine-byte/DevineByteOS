package io.devinebyte.runtime.event.diagnostics;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class EventDiagnostics {
    private final DiagnosticCollector collector;

    @Inject
    public EventDiagnostics(DiagnosticCollector collector) {
        this.collector = collector;
    }

    public void add(TenantContext ctx, String code, String title, String detail) {
        collector.error(code, title + ": " + detail, ctx.tenantId());
    }
}
