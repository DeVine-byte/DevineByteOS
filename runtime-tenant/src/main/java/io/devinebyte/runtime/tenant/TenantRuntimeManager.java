package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.tenant.registry.TenantRegistry;
import io.devinebyte.runtime.tenant.registry.TenantRuntimeHandle;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;

@Singleton
public class TenantRuntimeManager {
    private final RuntimeBootstrapper bootstrapper;
    private final TenantRuntimeFactory factory;
    private final TenantRegistry registry;

    @Inject
    public TenantRuntimeManager(RuntimeBootstrapper bootstrapper, TenantRuntimeFactory factory, TenantRegistry registry) {
        this.bootstrapper = bootstrapper;
        this.factory = factory;
        this.registry = registry;
    }

    public TenantRuntimeHandle bootTenant(Path dbpkg, String tenantId, boolean skipVerify) throws Exception {
        TenantContext initialCtx = new TenantContext(tenantId, TenantLifecycle.PROVISIONING, Set.of());
        DiagnosticCollector diagnostics = new DiagnosticCollector();

        try (FileSystem fs = FileSystems.newFileSystem(dbpkg)) {
            var bootstrap = bootstrapper.boot(initialCtx, dbpkg);
            if (!bootstrap.success() || bootstrap.diagnostics().hasFatal()) {
                bootstrap.diagnostics().getAll().forEach(d -> diagnostics.add(d));
                throw new IllegalStateException("Bootstrap failed: " + diagnostics.getAll());
            }

            TenantRuntime runtime = factory.create(bootstrap.tenantContext(), bootstrap, fs, diagnostics);
            runtime.boot();
            return registry.register(tenantId, runtime);
        }
    }

    public void shutdownTenant(String tenantId) {
        registry.unregister(tenantId);
    }
}
