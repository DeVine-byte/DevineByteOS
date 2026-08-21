package io.devinebyte.runtime.tenant;

import com.fasterxml.jackson.databind.ObjectMapper; // ADD
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // ADD
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.Path;

@Singleton
public class RuntimeLauncher {
    private final TenantRuntimeManager manager;

    @Inject
    public RuntimeLauncher(TenantRuntimeManager manager) { this.manager = manager; }

    public static void launch(Path dbpkg, String tenantId, boolean skipVerify) throws Exception {
        // Build shared dependencies
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var diagnostics = new io.devinebyte.runtime.core.diagnostics.DiagnosticCollector();
        
        var launcher = new RuntimeLauncher(new TenantRuntimeManager(
            new io.devinebyte.runtime.bootstrap.RuntimeBootstrapper(
                new io.devinebyte.runtime.bootstrap.DbpkgVerifier(skipVerify),
                new io.devinebyte.runtime.bootstrap.ManifestReader(),
                new io.devinebyte.runtime.module.ModuleLoader(diagnostics),
                new io.devinebyte.runtime.module.ModuleRegistry()
            ),
            new TenantRuntimeFactory(
                new io.devinebyte.runtime.config.ConfigurationManager(mapper), // 1
                mapper, // 2. ADD THIS
                new io.devinebyte.runtime.module.ModuleLoader(diagnostics), // 3
                new io.devinebyte.runtime.module.ModuleRegistry(), // 4
                new io.devinebyte.runtime.core.registry.RuntimeRegistry() // 5
            ),
            new io.devinebyte.runtime.tenant.registry.TenantRegistry()
        ));
        launcher.manager.bootTenant(dbpkg, tenantId, skipVerify);
    }
}
