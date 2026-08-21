package io.devinebyte.runtime.tenant.lifecycle;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TenantLifecycleController {
    private final DiagnosticCollector diagnostics;

    @Inject
    public TenantLifecycleController(DiagnosticCollector diagnostics) {
        this.diagnostics = diagnostics;
    }

    public LifecycleTransition transition(TenantContext ctx, TenantLifecycle target) {
        TenantLifecycle from = ctx.state();
        diagnostics.add(new io.devinebyte.runtime.core.diagnostics.Diagnostic(
            "LIFECYCLE_001", io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity.INFO,
            "Transition " + from + " -> " + target, ctx.tenantId(), null
        ));
        return new LifecycleTransition(ctx.tenantId(), from, target, java.time.Instant.now());
    }
}
