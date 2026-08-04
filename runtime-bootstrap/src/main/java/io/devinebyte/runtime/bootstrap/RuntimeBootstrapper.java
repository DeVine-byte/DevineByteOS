package io.devinebyte.runtime.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition; // FIX: import from config, not module
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipFile;

@Singleton
public class RuntimeBootstrapper {
    private final DbpkgVerifier verifier;
    private final ManifestReader manifestReader;
    private final ModuleLoader moduleLoader;
    private final ModuleRegistry moduleRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public RuntimeBootstrapper(
        DbpkgVerifier verifier,
        ManifestReader manifestReader,
        ModuleLoader moduleLoader,
        ModuleRegistry moduleRegistry
    ) {
        this.verifier = verifier;
        this.manifestReader = manifestReader;
        this.moduleLoader = moduleLoader;
        this.moduleRegistry = moduleRegistry;
    }

    public BootstrapResult boot(TenantContext tenant, Path dbpkgPath) {
        DiagnosticCollector diagnostics = new DiagnosticCollector();

        if (!verifier.verifyStructure(tenant, dbpkgPath, diagnostics)) {
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics);
        }

        try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
            var manifestEntry = zip.getEntry("manifest.json");
            var manifest = manifestReader.read(tenant, zip.getInputStream(manifestEntry), diagnostics);
            if (manifest == null || diagnostics.hasFatal()) {
                return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics);
            }

            // 1. Strict tenant check only if multiTenant=false
            if (!manifest.multiTenant() && !manifest.tenantId().equals(tenant.tenantId())) {
                diagnostics.fatal("DBRT007",
                    "Tenant mismatch. Expected: " + manifest.tenantId() + " Got: " + tenant.tenantId(),
                    tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics);
            }

            if (!verifier.verifyChecksum(tenant, dbpkgPath, manifest.checksumSha256(), diagnostics)) {
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics);
            }

            // 2. NEW: Load module_graph.json and resolve modules
            var moduleGraphEntry = zip.getEntry("runtime/module_graph.json");
            if (moduleGraphEntry == null) {
                diagnostics.fatal("DBRT004", "Missing required file: runtime/module_graph.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics);
            }

            ModuleGraph moduleGraph;
            try (InputStream is = zip.getInputStream(moduleGraphEntry)) {
                moduleGraph = objectMapper.readValue(is, ModuleGraph.class);
            }

            // 3. NEW: Load and topo-sort enabled modules. Fails on PKG003 cycle
            ModuleLoader.LoadResult loadResult = moduleLoader.load(tenant, moduleGraph, diagnostics);
            if (diagnostics.hasFatal()) {
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics);
            }

            // 4. NEW: Rebuild TenantContext with actual enabledModules from .dbpkg
            TenantContext bootContext = new TenantContext(
                tenant.tenantId(),
                tenant.state(),
                loadResult.enabledModules()
            );

            // 5. NEW: Register modules for isolation guard
            Map<String, ModuleDefinition> moduleMap = moduleGraph.modules(); // now compiles
            moduleRegistry.register(bootContext, moduleMap);

            return new BootstrapResult(true, bootContext, manifest, dbpkgPath, diagnostics);

        } catch (Exception e) {
            diagnostics.fatal("DBRT008", "Bootstrap failed: " + e.getMessage(), tenant.tenantId());
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics);
        }
    }
}
