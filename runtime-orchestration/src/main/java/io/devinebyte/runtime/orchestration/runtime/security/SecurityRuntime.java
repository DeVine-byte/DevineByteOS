package io.devinebyte.runtime.orchestration.runtime.security;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.core.diagnostics.Diagnostic;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.runtime.orchestration.runtime.api.ContractRouteRegistry;
import java.time.Instant;

public record SecurityRuntime(
    PermissionEngine permissionEngine,
    ModuleRegistry moduleRegistry,
    ContractRouteRegistry routeRegistry // ADDED
) {
    public void assertContractOnly(TenantContext ctx, String method, String path, DiagnosticCollector diag) {
        // CHANGED: Using instance variable routeRegistry instead of static reference
        if (!routeRegistry.isContractPath(method, path)) {
            diag.add(new Diagnostic(
                "DBRT001",
                DiagnosticSeverity.FATAL,
                "Path not in APISchema.json: " + method + " " + path,
                ctx.tenantId(),
                Instant.now()
            ));
            throw new SecurityException("Contract violation DBRT001");
        }
    }

    public void assertModuleEnabled(TenantContext ctx, String moduleId, DiagnosticCollector diag) {
        if (!moduleRegistry.isEnabled(ctx, moduleId)) {
            diag.add(new Diagnostic(
                "DBRT002",
                DiagnosticSeverity.FATAL,
                "Module disabled: " + moduleId,
                ctx.tenantId(),
                Instant.now()
            ));
            throw new IllegalStateException("Module disabled DBRT002");
        }
    }
}
