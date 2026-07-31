package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.BootstrapResult;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.config.FeatureFlags;
import io.devinebyte.runtime.config.ManifestDTO;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.TenantConfig;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.tenant.exception.TenantLifecycleException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Set;

@Singleton
public class TenantRuntimeFactory {
    private final ConfigurationManager configManager;

    @Inject
    public TenantRuntimeFactory(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    public TenantRuntime create(TenantContext tenant, BootstrapResult bootstrap) {
        DiagnosticCollector diagnostics = bootstrap.diagnostics();

        try {
            // Convert Manifest to DTO to avoid circular dep
            var m = bootstrap.manifest();
            ManifestDTO dto = new ManifestDTO(
                m.schemaVersion(), m.tenantId(), m.version(), 
                m.builtAt(), m.builtBy(), m.checksumSha256(), m.signature()
            );

            TenantConfig config = configManager.createTenantConfig(tenant.tenantId(), dto);
            FeatureFlags flags = configManager.createFeatureFlags(dto);
            ModuleGraph graph = configManager.createModuleGraph(dto, tenant);

            // Your ModuleGraph is Map<String, ModuleDefinition>. No helper method yet.
            Set<String> enabled = Set.of(); 
            TenantContext scopedContext = new TenantContext(tenant.tenantId(), tenant.state(), enabled);

            return new TenantRuntime(scopedContext, config, graph, flags, bootstrap.dbpkgPath());

        } catch (Exception e) {
            diagnostics.fatal("DBRT006", "Failed to create TenantRuntime: " + e.getMessage(), tenant.tenantId());
            throw new TenantLifecycleException("DBRT006", e);
        }
    }
}
