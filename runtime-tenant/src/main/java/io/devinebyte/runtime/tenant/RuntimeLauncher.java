package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.tenant.http.JdkHttpAdapter; 
import io.devinebyte.runtime.tenant.registry.TenantRegistry;
import io.devinebyte.runtime.tenant.registry.TenantRuntimeHandle;
import io.devinebyte.runtime.tenant.TenantRuntime;

import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

public class RuntimeLauncher {

    // Move adapter instantiation to an isolated, lazy holder pattern 
    // This allows multi-tenant test rigs to boot multiple bundles without breaking port 8080
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
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules(); // FIXED
        ConfigurationManager config = new ConfigurationManager(mapper);

        DiagnosticCollector diagnostics = new DiagnosticCollector();
        ModuleLoader loader = new ModuleLoader(diagnostics);
        ModuleRegistry registry = new ModuleRegistry();
        RuntimeRegistry runtimeRegistry = new RuntimeRegistry();

        DbpkgVerifier verifier = new DbpkgVerifier();
        ManifestReader manifestReader = new ManifestReader();

        TenantRuntimeFactory factory = new TenantRuntimeFactory(config, mapper, loader, registry, runtimeRegistry);
        RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(verifier, manifestReader, loader, registry);

        TenantRegistry tenantRegistry = new TenantRegistry();
        TenantRuntimeManager manager = new TenantRuntimeManager(bootstrapper, factory, tenantRegistry);

        TenantRuntimeHandle handle = manager.bootTenant(dbpkg, tenantId, skipVerify);
        TenantRuntime runtime = handle.runtime();
        runtime.boot();

        // WIRE HTTP SERVER (Idempotent socket instantiation)
        JdkHttpAdapter http = getHttpAdapter(8080);
        http.registerTenant(runtime); 

        // ADD GRACEFUL SHUTDOWN HOOK
        // This flushes internal diagnostics logs and clears network traffic when Ctrl+C is caught
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[DBRT] Shutting down cleanly...");
            try {
                runtime.close(); // Cleans up file system hooks, tracks BOOT_002 diagnostic event
            } catch (Exception e) {
                System.err.println("[DBRT] Error releasing tenant resources: " + e.getMessage());
            }
        }));

        System.out.println("[DBRT] Tenant " + tenantId + " online. Press Ctrl+C to stop");
        Thread.currentThread().join();
    }
}
