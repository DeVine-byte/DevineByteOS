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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

public class RuntimeLauncher {

    private static JdkHttpAdapter sharedHttpAdapter;

    private static synchronized JdkHttpAdapter getHttpAdapter(int port) throws Exception {
        if (sharedHttpAdapter == null) {
            sharedHttpAdapter = new JdkHttpAdapter(port);
            sharedHttpAdapter.start();
        }
        return sharedHttpAdapter;
    }

    public static void launch(Path dbpkg, String tenantId, boolean skipVerify) throws Exception {
        runInternal(dbpkg, tenantId, skipVerify);
    }

    public static void main(String[] args) throws Exception {
        boolean skipVerify = java.util.Arrays.asList(args).contains("--skip-verify");
        String dbpkg = null, tenantId = null;
        for (int i = 0; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) dbpkg = args[i + 1];
            if ("--tenant".equals(args[i]) && i + 1 < args.length) tenantId = args[i + 1];
        }
        if (dbpkg == null || tenantId == null) throw new IllegalArgumentException("Usage: run --dbpkg <path> --tenant <id> [--skip-verify]");
        runInternal(Path.of(dbpkg), tenantId, skipVerify);
    }

    private static void runInternal(Path dbpkg, String tenantId, boolean skipVerify) throws Exception {
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
        // FIXED: RE-ROUTE HOOK REGISTRATION VIA THE HANDLER REGISTRY
        // ==========================================================
        HandlerRegistry handlerRegistry = new HandlerRegistry();
        
        // Dynamically register our asynchronous notification centers matching the lifecycle events matrix
        handlerRegistry.register(new NotificationCenterHandler("AppointmentCreated"));
        handlerRegistry.register(new NotificationCenterHandler("AppointmentUpdated"));
        handlerRegistry.register(new NotificationCenterHandler("PaymentConfirmed"));

        TenantRuntimeFactory factory = new TenantRuntimeFactory(config, mapper, loader, registry, runtimeRegistry, workflowEngine);
        RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(verifier, manifestReader, loader, registry, workflowEngine);

        TenantRegistry tenantRegistry = new TenantRegistry();
        TenantRuntimeManager manager = new TenantRuntimeManager(bootstrapper, factory, tenantRegistry);

        TenantRuntimeHandle handle = manager.bootTenant(dbpkg, tenantId, skipVerify);
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
}

