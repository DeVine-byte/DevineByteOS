package io.devinebyte.runtime.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.plugin.DbpkgExtractor; // NEW
import io.devinebyte.runtime.plugin.PluginContext;
import io.devinebyte.runtime.plugin.PluginLoader;
import io.devinebyte.runtime.plugin.PluginManifest;
import io.devinebyte.runtime.plugin.RuntimePlugin;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        List<ApiSchema> apiSchemas = List.of();

        if (!verifier.verifyStructure(tenant, dbpkgPath, diagnostics)) {
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
        }

        try (ZipFile zip = new ZipFile(dbpkgPath.toFile())) {
            var manifestEntry = zip.getEntry("manifest.json");
            if (manifestEntry == null) {
                diagnostics.fatal("DBRT005", "Missing required file: manifest.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
            }

            var manifest = manifestReader.read(tenant, zip.getInputStream(manifestEntry), diagnostics);
            if (manifest == null || diagnostics.hasFatal()) {
                return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
            }

            if (!manifest.multiTenant() && !manifest.tenantId().equals(tenant.tenantId())) {
                diagnostics.fatal("DBRT007", "Tenant mismatch. Expected: " + manifest.tenantId() + " Got: " + tenant.tenantId(), tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            if (!verifier.verifyChecksum(tenant, dbpkgPath, manifest.checksumSha256(), diagnostics)) {
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            // 1. Load APISchema.json
            var apiSchemaEntry = zip.getEntry("contracts/APISchema.json");
            if (apiSchemaEntry == null) {
                diagnostics.fatal("DBRT005", "Missing required file: contracts/APISchema.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }
            try (InputStream is = zip.getInputStream(apiSchemaEntry)) {
                apiSchemas = objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, ApiSchema.class));
            }

            // 2. Load module_graph.json
            var moduleGraphEntry = zip.getEntry("runtime/module_graph.json");
            if (moduleGraphEntry == null) {
                diagnostics.fatal("DBRT004", "Missing required file: runtime/module_graph.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            ModuleGraph moduleGraph;
            try (InputStream is = zip.getInputStream(moduleGraphEntry)) {
                moduleGraph = objectMapper.readValue(is, ModuleGraph.class);
            }

            ModuleLoader.LoadResult loadResult = moduleLoader.load(tenant, moduleGraph, diagnostics);
            if (diagnostics.hasFatal()) {
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            TenantContext bootContext = new TenantContext(tenant.tenantId(), tenant.state(), loadResult.enabledModules());
            Map<String, ModuleDefinition> moduleMap = moduleGraph.modules();
            moduleRegistry.register(bootContext, moduleMap);

            // 3. Load Plugins from /bootstrap/plugins/
            Path tempExtractDir = Files.createTempDirectory("dbos-" + tenant.tenantId());
            DbpkgExtractor.extract(zip, tempExtractDir, diagnostics);

            Path pluginsPath = tempExtractDir.resolve("bootstrap/plugins");
            if (Files.exists(pluginsPath)) {
                PluginManifest pluginManifest = readPluginManifest(pluginsPath, diagnostics);
                PluginContext pluginContext = new PluginContext(
                    bootContext, null, null, null, null, null, null // TODO: wire real EventBus, etc
                );
                PluginLoader loader = new PluginLoader(pluginsPath);
                List<RuntimePlugin> plugins = loader.loadPlugins(pluginContext, diagnostics);

                for (RuntimePlugin plugin : plugins) {
                    plugin.initialize(pluginContext, diagnostics);
                }
                for (RuntimePlugin plugin : plugins) {
                    plugin.start(diagnostics);
                }
                diagnostics.addInfo("BOOT_002", "Loaded " + plugins.size() + " plugins");
            }

            return new BootstrapResult(true, bootContext, manifest, dbpkgPath, diagnostics, apiSchemas);

        } catch (Exception e) {
            diagnostics.fatal("DBRT008", "Bootstrap failed: " + e.getMessage(), tenant.tenantId());
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
        }
    }

    private PluginManifest readPluginManifest(Path pluginsPath, DiagnosticCollector diagnostics) {
        try {
            return objectMapper.readValue(pluginsPath.resolve("manifest.json").toFile(), PluginManifest.class);
        } catch (Exception e) {
            diagnostics.fatal("DBRT150", "Failed to read plugin manifest: " + e.getMessage());
            return new PluginManifest(List.of());
        }
    }
}
