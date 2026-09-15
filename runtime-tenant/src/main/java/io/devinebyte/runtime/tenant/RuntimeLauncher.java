package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.tenant.http.JdkHttpAdapter;
import io.devinebyte.runtime.tenant.registry.TenantRegistry;
import io.devinebyte.runtime.tenant.registry.TenantRuntimeHandle;
import io.devinebyte.runtime.tenant.TenantRuntime;
import io.devinebyte.runtime.projection.kpi.KPIEngine;

import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import io.devinebyte.runtime.workflow.engine.WorkflowExecutor;
import io.devinebyte.runtime.event.handler.HandlerRegistry;
import io.devinebyte.runtime.event.handler.NotificationCenterHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RuntimeLauncher {

    private static JdkHttpAdapter sharedHttpAdapter;

    private static synchronized JdkHttpAdapter getHttpAdapter(int port) throws Exception {
        if (sharedHttpAdapter == null) {
            sharedHttpAdapter = new JdkHttpAdapter(port);
            sharedHttpAdapter.start();
        }
        return sharedHttpAdapter;
    }

    public static void launch(Path dbpkg, String tenantId) throws Exception {
        runInternal(dbpkg, tenantId);
    }

    public static void main(String[] args) throws Exception {
        String dbpkg = null, tenantId = null;
        for (int i = 0; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) dbpkg = args[i + 1];
            if ("--tenant".equals(args[i]) && i + 1 < args.length) tenantId = args[i + 1];
        }
        if (dbpkg == null || tenantId == null) throw new IllegalArgumentException("Usage: run --dbpkg <path> --tenant <id>");
        runInternal(Path.of(dbpkg), tenantId);
    }

    private static void runInternal(Path dbpkg, String tenantId) throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ConfigurationManager config = new ConfigurationManager(mapper);

        DiagnosticCollector diagnostics = new DiagnosticCollector();
        ModuleLoader loader = new ModuleLoader(diagnostics);
        ModuleRegistry registry = new ModuleRegistry();
        RuntimeRegistry runtimeRegistry = new RuntimeRegistry();

        DbpkgVerifier verifier = new DbpkgVerifier();
        ManifestReader manifestReader = new ManifestReader();

        // Construct the single source of truth workflow engine reference
        WorkflowExecutor executor = new WorkflowExecutor(null, null);
        KPIEngine kpiEngine = new KPIEngine();
        WorkflowEngine workflowEngine = new WorkflowEngine(null, executor, kpiEngine);

        // ==========================================================
        // DYNAMIC: RE-ROUTE HOOK REGISTRATION DOMAIN-BLIND STRATEGY
        // ==========================================================
        HandlerRegistry handlerRegistry = new HandlerRegistry();
        
        // Dynamically discover what events this tenant has configured notifications for
        List<String> dynamicTriggers = discoverEventTriggersForTenant(tenantId, mapper);
        
        for (String eventType : dynamicTriggers) {
            handlerRegistry.register(new NotificationCenterHandler(eventType));
        }

        TenantRuntimeFactory factory = new TenantRuntimeFactory(config, mapper, loader, registry, runtimeRegistry, workflowEngine);
        RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(verifier, manifestReader, loader, registry, workflowEngine);

        TenantRegistry tenantRegistry = new TenantRegistry();
        TenantRuntimeManager manager = new TenantRuntimeManager(bootstrapper, factory, tenantRegistry);

        // FIXED: Invoking the clean 2-argument verification signature pattern
        TenantRuntimeHandle handle = manager.bootTenant(dbpkg, tenantId);
        TenantRuntime runtime = handle.runtime();
        runtime.boot();

        // WIRE HTTP SERVER 
        JdkHttpAdapter http = getHttpAdapter(8080);
        http.registerTenant(runtime);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[DBRT] Shutting down cleanly...");
            try {
                runtime.close();
            } catch (Exception e) {
                System.err.println("[DBRT] Error releasing tenant resources: " + e.getMessage());
            }
        }));

        System.out.println("[DBRT] Tenant " + tenantId + " online. Press Ctrl+C to stop");
        Thread.currentThread().join();
    }

    /**
     * Replicates NotificationCenterHandler's multi-path scanner strategy out-of-band.
     * Extracts active triggers from the tenant notification profiles to keep infrastructure domain-blind.
     */
    private static List<String> discoverEventTriggersForTenant(String tenantId, ObjectMapper mapper) {
        List<String> triggers = new ArrayList<>();
        String relTemplates = "data/tenants/" + tenantId + "/notification_templates.json";

        File templateFile = new File(relTemplates);
        if (!templateFile.exists()) templateFile = new File("../" + relTemplates);
        if (!templateFile.exists()) templateFile = new File("../../" + relTemplates);
        if (!templateFile.exists()) templateFile = new File("/data/data/com.termux/files/home/DevineByteOS/" + relTemplates);

        if (!templateFile.exists()) {
            System.out.println("[DBRT LAUNCHER] Baseline profile scan completed. No local out-of-band notification layouts defined.");
            return triggers;
        }

        try {
            JsonNode root = mapper.readTree(Files.readAllBytes(templateFile.toPath()));
            JsonNode templates = root.get("templates");
            if (templates != null && templates.isArray()) {
                for (JsonNode node : templates) {
                    JsonNode triggerNode = node.get("eventTrigger");
                    if (triggerNode != null) {
                        String eventType = triggerNode.asText();
                        if (!triggers.contains(eventType)) {
                            triggers.add(eventType);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DBRT LAUNCHER WARN] Error scanning template configurations dynamically: " + e.getMessage());
        }
        return triggers;
    }
}

