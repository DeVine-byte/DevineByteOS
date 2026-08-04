package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.BootstrapResult;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.config.FeatureFlags;
import io.devinebyte.runtime.config.ManifestDTO;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.TenantConfig;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.tenant.exception.TenantLifecycleException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TenantRuntimeFactory {
    private final ConfigurationManager configManager;
    private final ModuleLoader moduleLoader;
    private final ModuleRegistry moduleRegistry;

    @Inject
    public TenantRuntimeFactory(
        ConfigurationManager configManager,
        ModuleLoader moduleLoader,
        ModuleRegistry moduleRegistry
    ) {
        this.configManager = configManager;
        this.moduleLoader = moduleLoader;
        this.moduleRegistry = moduleRegistry;
    }

    public TenantRuntime create(TenantContext bootstrapTenant, BootstrapResult bootstrap) {
        DiagnosticCollector diagnostics = bootstrap.diagnostics();

        try {
            // 1. Convert Manifest to DTO
            var m = bootstrap.manifest();
            ManifestDTO dto = new ManifestDTO(
                m.schemaVersion(), m.tenantId(), m.version(),
                m.builtAt(), m.builtBy(), m.checksumSha256(), m.signature()
            );

            // 2. Load config + flags from .dbpkg
            TenantConfig config = configManager.createTenantConfig(bootstrapTenant.tenantId(), dto);
            FeatureFlags flags = configManager.createFeatureFlags(dto);

            // 3. FIXED: Load ModuleGraph from .dbpkg using dbpkgPath
            ModuleGraph graph = configManager.createModuleGraphFromDbpkg(bootstrap.dbpkgPath());
            
            // 4. Resolve and load modules based on graph
            ModuleLoader.LoadResult loadResult = moduleLoader.load(bootstrapTenant, graph, diagnostics);

            if (diagnostics.hasFatal()) {
                throw new TenantLifecycleException("DBRT006",
                    new IllegalStateException("Module loading failed"));
            }

            // 5. Rebuild TenantContext with real enabledModules from .dbpkg
            TenantContext scopedContext = new TenantContext(
                bootstrapTenant.tenantId(),
                bootstrapTenant.state(),
                loadResult.enabledModules()
            );

            // 6. Register modules for ModuleIsolationGuard
            moduleRegistry.register(scopedContext, graph.modules());

            return new TenantRuntime(scopedContext, config, graph, flags, bootstrap.dbpkgPath());

        } catch (Exception e) {
            diagnostics.fatal("DBRT006", "Failed to create TenantRuntime: " + e.getMessage(), bootstrapTenant.tenantId());
            throw new TenantLifecycleException("DBRT006", e);
        }
    }
}
