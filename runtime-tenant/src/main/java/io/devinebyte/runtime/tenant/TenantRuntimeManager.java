package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.observability.PlatformObservabilityEngine;
import io.devinebyte.runtime.tenant.registry.TenantRegistry;
import io.devinebyte.runtime.tenant.registry.TenantRuntimeHandle;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public final class TenantRuntimeManager {
    private final RuntimeBootstrapper bootstrapper;
    private final TenantRuntimeFactory factory;
    private final TenantRegistry registry;
    private final PlatformObservabilityEngine otelEngine = new PlatformObservabilityEngine();

    public TenantRuntimeManager(RuntimeBootstrapper bootstrapper, TenantRuntimeFactory factory, TenantRegistry registry) {
        this.bootstrapper = bootstrapper;
        this.factory = factory;
        this.registry = registry;
    }

    public TenantRuntimeHandle bootTenant(String tenantId) throws Exception {
        Path resolvedDbpkg = DbpkgLocator.locate(tenantId);
        return bootTenant(resolvedDbpkg, tenantId);
    }

    public TenantRuntimeHandle bootTenant(Path dbpkg, String tenantId) throws Exception {
        // Start high-precision instrumentation clock tracking
        long startTimeNano = System.nanoTime();

        TenantContext initialCtx = new TenantContext(tenantId, TenantLifecycle.PROVISIONING, Set.of());
        DiagnosticCollector diagnostics = new DiagnosticCollector();

        var bootstrap = bootstrapper.boot(initialCtx, dbpkg);
        if (!bootstrap.success() || bootstrap.diagnostics().hasFatal()) {
            bootstrap.diagnostics().getAll().forEach(diagnostics::add);
            throw new IllegalStateException("Substrate Bootstrap Denied by Verification Engine: " + diagnostics.getAll());
        }

        try (FileSystem fs = FileSystems.newFileSystem(dbpkg)) {
            TenantRuntime runtime = factory.create(bootstrap.tenantContext(), bootstrap, fs, diagnostics);
            runtime.boot();
            
            // Calculate absolute execution latency out-of-band
            long endTimeNano = System.nanoTime();
            double durationSeconds = (endTimeNano - startTimeNano) / 1_000_000_000.0;
            long durationMs = (endTimeNano - startTimeNano) / 1_000_000;
            
            // Extract the true active module count registered inside the source of truth context object
            int activeModuleCount = bootstrap.tenantContext().enabledModules() != null 
                ? bootstrap.tenantContext().enabledModules().size() 
                : 0;

            // ==========================================================
            // ITEM 17 FIXED: DISPATCH OPENTELEMETRY BINDINGS
            // ==========================================================
            otelEngine.recordBootMetrics(tenantId, durationSeconds, activeModuleCount);
            otelEngine.recordTraceSpan("RUNTIME_BOOTSTRAP", tenantId, durationMs, Map.of(
                "dbpkg_path", dbpkg.getFileName().toString(),
                "status", "ACTIVE"
            ));

            return registry.register(tenantId, runtime);
        }
    }

    public void shutdownTenant(String tenantId) {
        registry.unregister(tenantId);
    }
}

