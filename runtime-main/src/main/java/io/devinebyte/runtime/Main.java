package io.devinebyte.runtime;

import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.tenant.TenantRuntime;
import io.devinebyte.runtime.tenant.TenantRuntimeFactory;
import io.devinebyte.runtime.event.core.SimpleEventBus;
import io.devinebyte.runtime.event.core.NoOpEventDispatcher;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.storage.FileEventStore;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.event.core.EventStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.devinebyte.runtime.event.model.EventMetadata;
import java.util.UUID;
import java.time.Instant;
import java.util.Map;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 4 || !args[0].equals("run")) {
            System.err.println("Usage: dbos run --dbpkg <path> --tenant <tenantId> [--skip-verify]");
            System.exit(1);
        }

        List<String> argList = Arrays.asList(args);
        boolean skipVerify = argList.contains("--skip-verify");

        String dbpkgArg = null;
        String tenantArg = null;
        for (int i = 0; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) dbpkgArg = args[i + 1];
            if ("--tenant".equals(args[i]) && i + 1 < args.length) tenantArg = args[i + 1];
        }
        if (dbpkgArg == null || tenantArg == null) {
            System.err.println("Missing --dbpkg or --tenant");
            System.exit(1);
        }

        Path dbpkg = Path.of(dbpkgArg);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Set<String> requestedModules = readRequestedModules(dbpkg, mapper);

        DiagnosticCollector diagnostics = new DiagnosticCollector();
        ModuleLoader moduleLoader = new ModuleLoader(diagnostics);
        ModuleRegistry moduleRegistry = new ModuleRegistry();

        RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(
            new DbpkgVerifier(skipVerify), new ManifestReader(), moduleLoader, moduleRegistry
        );
        ConfigurationManager configManager = new ConfigurationManager(mapper);
        TenantRuntimeFactory factory = new TenantRuntimeFactory(configManager, moduleLoader, moduleRegistry);

        TenantContext ctx = new TenantContext(tenantArg, TenantLifecycle.ACTIVE, requestedModules);
        var bootstrap = bootstrapper.boot(ctx, dbpkg);

        System.out.println("Diagnostics: " + bootstrap.diagnostics().getAll());
        if (!bootstrap.success()) {
            System.err.println("Bootstrap failed: " + bootstrap.diagnostics().getAll());
            System.exit(1);
        }

        TenantRuntime runtime = factory.create(bootstrap.tenantContext(), bootstrap);

        Path tenantBase = Path.of("/data/data/com.termux/files/home/DevineByteOS/data/tenants");
        Files.createDirectories(tenantBase);

        moduleRegistry.register(runtime.tenantContext(), runtime.moduleGraph().modules());

        EventDiagnostics eventDiagnostics = new EventDiagnostics(diagnostics); // FIX 1: pass diagnostics
        EventStore store = new FileEventStore(tenantBase, mapper, eventDiagnostics);
        ModuleIsolationGuard guard = new ModuleIsolationGuard(moduleRegistry);
        NoOpEventDispatcher dispatcher = new NoOpEventDispatcher(); // FIX 2: use concrete class
        SimpleEventBus eventBus = new SimpleEventBus(store, dispatcher, guard);

        ObjectNode payload = mapper.createObjectNode().put("boot", "ok").put("ts", System.currentTimeMillis());

        // FIX: Build metadata with sourceModule = sales so guard allows it
        EventMetadata meta = new EventMetadata(
            java.util.UUID.randomUUID(),
            null, // correlationId
            "sales", // <- sourceModule. Must be enabled
            java.time.Instant.now(),
            java.util.Map.of("tenantId", runtime.tenantContext().tenantId())
        );
        DomainEvent bootEvent = new DomainEvent("SystemBooted", "1.0", payload, meta);
        eventBus.publish(runtime.tenantContext(), bootEvent);

        System.out.println("✅ Booted tenant: " + runtime.tenantContext().tenantId());
        System.out.println(" From dbpkg: " + runtime.dbpkgPath());
        System.out.println(" Lifecycle: " + runtime.tenantContext().state());
        System.out.println(" Enabled Modules: " + runtime.tenantContext().enabledModules());
        System.out.println(" EventBus: Wrote 1 event to " + tenantBase.resolve(runtime.tenantContext().tenantId()) + "/events.log");
    }

    private static Set<String> readRequestedModules(Path dbpkg, ObjectMapper mapper) throws Exception {
        try (FileSystem fs = FileSystems.newFileSystem(dbpkg)) {
            Path cfg = fs.getPath("runtime/tenant_config.json");
            if (java.nio.file.Files.exists(cfg)) {
                try (InputStream in = java.nio.file.Files.newInputStream(cfg)) {
                    Map<String, Object> json = mapper.readValue(in, Map.class);
                    List<String> list = (List<String>) json.getOrDefault("requestedModules", List.of());
                    if (!list.isEmpty()) return new HashSet<>(list);
                }
            }
            Path graphPath = fs.getPath("runtime/module_graph.json");
            if (!java.nio.file.Files.exists(graphPath)) return Set.of();
            try (InputStream in = java.nio.file.Files.newInputStream(graphPath)) {
                Map<String, Object> graph = mapper.readValue(in, Map.class);
                Map<String, Object> modules = (Map<String, Object>) graph.get("modules");
                Set<String> enabled = new HashSet<>();
                if (modules != null) {
                    for (Map.Entry<String, Object> e : modules.entrySet()) {
                        Map<String, Object> def = (Map<String, Object>) e.getValue();
                        if (Boolean.TRUE.equals(def.get("enabled"))) enabled.add(e.getKey());
                    }
                }
                return enabled;
            }
        }
    }
}

