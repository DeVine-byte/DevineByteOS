package io.devinebyte.runtime.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

public final class RuntimeBootstrapper {
    private final DbpkgVerifier verifier;
    private final ManifestReader manifestReader;
    private final ModuleLoader moduleLoader;
    private final ModuleRegistry moduleRegistry;
    private final WorkflowEngine workflowEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RuntimeBootstrapper(
        DbpkgVerifier verifier,
        ManifestReader manifestReader,
        ModuleLoader moduleLoader,
        ModuleRegistry moduleRegistry,
        WorkflowEngine workflowEngine
    ) {
        this.verifier = verifier;
        this.manifestReader = manifestReader;
        this.moduleLoader = moduleLoader;
        this.moduleRegistry = moduleRegistry;
        this.workflowEngine = workflowEngine;
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
                diagnostics.fatal("DBRT005", "Security Failure: Missing critical root deployment metadata manifest.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
            }

            var manifest = manifestReader.read(tenant, zip.getInputStream(manifestEntry), diagnostics);
            if (manifest == null || diagnostics.hasFatal()) {
                return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
            }

            if (!verifier.verifyChecksum(tenant, dbpkgPath, manifest.sha256(), diagnostics)) {
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            var apiSchemaEntry = zip.getEntry("contracts/APISchema.json");
            if (apiSchemaEntry == null) {
                diagnostics.fatal("DBRT005", "Structure Failure: Missing gateway access mapping definitions /contracts/APISchema.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }
            try (InputStream is = zip.getInputStream(apiSchemaEntry)) {
                apiSchemas = objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, ApiSchema.class));
            }

            var moduleGraphEntry = zip.getEntry("runtime/module_graph.json");
            if (moduleGraphEntry == null) {
                diagnostics.fatal("DBRT004", "Structure Failure: Missing module topology maps /runtime/module_graph.json", tenant.tenantId());
                return new BootstrapResult(false, tenant, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            ModuleGraph moduleGraph;
            try (InputStream is = zip.getInputStream(moduleGraphEntry)) {
                moduleGraph = objectMapper.readValue(is, ModuleGraph.class);
            }

            // ==========================================================
            // ITEM 6 FIXED: EXTRACT SOURCE OF TRUTH CONTEXT FIRST
            // ==========================================================
            Set<String> activeModules = new HashSet<>();
            if (manifest.enabledModules() != null && !manifest.enabledModules().isEmpty()) {
                activeModules.addAll(manifest.enabledModules());
            } else {
                // Fallback Strategy: Scan module graph layout flags directly for enabled modules
                for (Map.Entry<String, ModuleDefinition> entry : moduleGraph.modules().entrySet()) {
                    if (entry.getValue().enabled()) {
                        activeModules.add(entry.getKey());
                    }
                }
            }

            // Construct our pristine, single source of truth context object
            TenantContext bootContext = new TenantContext(tenant.tenantId(), tenant.state(), activeModules);

            // Pass the derived authenticated bootContext down to trigger verification scanning sequences
            moduleLoader.load(bootContext, moduleGraph, diagnostics);
            if (diagnostics.hasFatal()) {
                return new BootstrapResult(false, bootContext, manifest, dbpkgPath, diagnostics, apiSchemas);
            }

            Map<String, ModuleDefinition> moduleMap = moduleGraph.modules();
            moduleRegistry.register(bootContext, moduleMap);

            for (WorkflowDefinition def : loadWorkflowsFromDbpkg(zip, diagnostics)) {
                workflowEngine.register(def);
            }

            return new BootstrapResult(true, bootContext, manifest, dbpkgPath, diagnostics, apiSchemas);

        } catch (Exception e) {
            diagnostics.fatal("DBRT008", "Substrate Core Engine Bootstrap Crashed Out: " + e.getMessage(), tenant.tenantId());
            return new BootstrapResult(false, tenant, null, dbpkgPath, diagnostics, apiSchemas);
        }
    }

    private List<WorkflowDefinition> loadWorkflowsFromDbpkg(ZipFile zip, DiagnosticCollector diagnostics) {
        List<WorkflowDefinition> defs = new java.util.ArrayList<>();
        var workflowsEntry = zip.getEntry("workflows/compiled_state_machines.json");
        if (workflowsEntry == null) workflowsEntry = zip.getEntry("compiled_state_machines.json");

        if (workflowsEntry != null) {
            try (InputStream is = zip.getInputStream(workflowsEntry)) {
                List<WorkflowDefinition> parsed = objectMapper.readValue(
                    is, objectMapper.getTypeFactory().constructCollectionType(List.class, WorkflowDefinition.class)
                );
                if (parsed != null) defs.addAll(parsed);
            } catch (Exception ignored) {}
        }
        return defs;
    }
}

