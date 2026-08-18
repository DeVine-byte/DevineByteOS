package io.devinebyte.runtime;

import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.tenant.TenantRuntime;
import io.devinebyte.runtime.tenant.TenantRuntimeFactory;
import io.devinebyte.runtime.config.ModuleGraph;

import io.devinebyte.runtime.event.core.SimpleEventBus;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.EventMetadata;
import io.devinebyte.runtime.event.storage.FileEventStore;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.core.EventDispatcher;
import io.devinebyte.runtime.event.core.CompositeEventDispatcher;
import io.devinebyte.runtime.event.handler.HandlerRegistry;

import io.devinebyte.runtime.projection.engine.WasmRuntime;
import io.devinebyte.runtime.projection.engine.ProjectionEngine;
import io.devinebyte.runtime.projection.diagnostics.ProjectionDiagnostics;
import io.devinebyte.runtime.projection.loader.ProjectionLoader;
import io.devinebyte.runtime.projection.loader.ProjectionLoadResult;
import io.devinebyte.runtime.projection.store.FileProjectionStateStore;
import io.devinebyte.runtime.projection.handler.ProjectionEventHandler;
import io.devinebyte.compiler.projection.model.ProjectionFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.UUID;
import java.time.Instant;
import java.util.Map;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 4 ||!args[0].equals("run")) {
            System.err.println("Usage: dbos run --dbpkg <path> --tenant <tenantId> [--skip-verify]");
            System.exit(1);
        }

        boolean skipVerify = Arrays.asList(args).contains("--skip-verify");
        String dbpkgArg = null, tenantArg = null;
        for (int i = 0; i < args.length; i++) {
            if ("--dbpkg".equals(args[i]) && i + 1 < args.length) dbpkgArg = args[i + 1];
            if ("--tenant".equals(args[i]) && i + 1 < args.length) tenantArg = args[i + 1];
        }
        if (dbpkgArg == null || tenantArg == null) {
            System.err.println("Missing --dbpkg or --tenant");
            System.exit(1);
        }

        Path dbpkg = Path.of(dbpkgArg);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try (FileSystem fs = FileSystems.newFileSystem(dbpkg)) {
            Set<String> requestedModules = readRequestedModules(fs, mapper);

            DiagnosticCollector diagnostics = new DiagnosticCollector();
            ModuleLoader moduleLoader = new ModuleLoader(diagnostics);
            ModuleRegistry moduleRegistry = new ModuleRegistry();

            RuntimeBootstrapper bootstrapper = new RuntimeBootstrapper(
                    new DbpkgVerifier(skipVerify), new ManifestReader(), moduleLoader, moduleRegistry
            );
            ConfigurationManager configManager = new ConfigurationManager(mapper);
            TenantRuntimeFactory factory = new TenantRuntimeFactory(configManager, moduleLoader, moduleRegistry);

            TenantContext tenantCtx = new TenantContext(tenantArg, TenantLifecycle.ACTIVE, requestedModules);
            var bootstrap = bootstrapper.boot(tenantCtx, dbpkg);

            if (bootstrap.diagnostics().hasFatal()) {
                System.err.println("Bootstrap failed: " + bootstrap.diagnostics().getAll());
                System.exit(1);
            }

            // FIX: Override context because bootstrap returns empty modules
            TenantContext fixedCtx = new TenantContext(
                    bootstrap.tenantContext().tenantId(),
                    TenantLifecycle.ACTIVE,
                    requestedModules
            );

            TenantRuntime runtime = factory.create(fixedCtx, bootstrap);
            Path tenantBase = Path.of("build/data/tenants");
            Files.createDirectories(tenantBase);

            // FIX: Build ModuleGraph with dependencies so guard is happy
            ModuleGraph moduleGraph = buildModuleGraph(fs, requestedModules, mapper);
            moduleRegistry.register(fixedCtx, moduleGraph.modules());

            EventDiagnostics eventDiagnostics = new EventDiagnostics(diagnostics);
            EventStore store = new FileEventStore(tenantBase, mapper, eventDiagnostics);
            ModuleIsolationGuard guard = new ModuleIsolationGuard(moduleRegistry);

            HandlerRegistry handlerRegistry = new HandlerRegistry();
            EventDispatcher baseDispatcher = new EventDispatcher(handlerRegistry, guard);

            // Projection wiring
            if (fixedCtx.enabledModules().contains("projection")) {
                ProjectionLoader loader = new ProjectionLoader(mapper);
                ProjectionLoadResult projectionLoad = loader.load(fs, diagnostics);
                boolean hasErrors = projectionLoad.diagnostics().getAll().stream()
                      .anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR || d.severity() == DiagnosticSeverity.FATAL);
                if (!hasErrors) {
                    WasmRuntime wasm = new WasmRuntime(mapper);
                    FileProjectionStateStore stateStore = new FileProjectionStateStore();
                    ProjectionDiagnostics projDiag = new ProjectionDiagnostics(diagnostics);
                    ProjectionEngine projectionEngine = new ProjectionEngine(wasm, stateStore, guard, projDiag);
                    ProjectionEventHandler projectionHandler = new ProjectionEventHandler(projectionEngine, fixedCtx, projectionLoad);
                    handlerRegistry.register(projectionHandler);
                    System.out.println("✅ ProjectionEngine loaded: " + projectionLoad.functions().size() + " functions");
                } else {
                    System.err.println("Projection load failed: " + projectionLoad.diagnostics().getAll());
                }
            }

            CompositeEventDispatcher dispatcher = new CompositeEventDispatcher(List.of(baseDispatcher));
            SimpleEventBus eventBus = new SimpleEventBus(store, dispatcher, guard);

            // Test events
            ObjectNode payload = mapper.createObjectNode().put("boot", "ok");
            EventMetadata meta = new EventMetadata(UUID.randomUUID(), null, "runtime", Instant.now(),
                    Map.of("tenantId", fixedCtx.tenantId()));
            DomainEvent bootEvent = new DomainEvent("SystemBooted", "1.0", payload, meta);
            eventBus.publish(fixedCtx, bootEvent);

            ObjectNode orderPayload = mapper.createObjectNode().put("orderId", "ORD-123").put("amount", 99.99);
            DomainEvent orderEvent = new DomainEvent("sales.OrderCreated", "1.0", orderPayload, meta);
            eventBus.publish(fixedCtx, orderEvent);

            System.out.println("✅ Booted tenant: " + fixedCtx.tenantId());
            System.out.println(" Enabled Modules: " + fixedCtx.enabledModules());
        }
    }

    private static Set<String> readRequestedModules(FileSystem fs, ObjectMapper mapper) throws Exception {
        Set<String> modules = new HashSet<>();

        Path manifestPath = fs.getPath("/manifest.json");
        if (Files.exists(manifestPath)) {
            try (InputStream in = Files.newInputStream(manifestPath)) {
                Map<String, Object> manifest = mapper.readValue(in, Map.class);
                System.out.println("[INFO] Loaded manifest for tenant: " + manifest.get("tenantId"));
            }
        }

        Path cfg = fs.getPath("runtime/tenant_config.json");
        if (Files.exists(cfg)) {
            try (InputStream in = Files.newInputStream(cfg)) {
                Map<String, Object> json = mapper.readValue(in, Map.class);
                Object req = json.get("requestedModules");
                if (req instanceof List) {
                    modules.addAll((List<String>) req);
                } else if (req!= null) {
                    modules.addAll(Arrays.asList(req.toString().split(",")));
                }
            }
        }

        // CRITICAL: Always inject system runtime module
        modules.add("runtime");
        System.out.println("Enabled Modules from dbpkg: " + modules);
        return modules;
    }

    private static ModuleGraph buildModuleGraph(FileSystem fs, Set<String> requestedModules, ObjectMapper mapper) throws Exception {
        Map<String, ModuleGraph.ModuleDefinition> defs = new HashMap<>();

        Path graphPath = fs.getPath("runtime/module_graph.json");
        if (Files.exists(graphPath)) {
            try (InputStream in = Files.newInputStream(graphPath)) {
                Map<String, Object> json = mapper.readValue(in, Map.class);
                Map<String, Map<String, Object>> modules = (Map<String, Map<String, Object>>) json.get("modules");

                System.out.println("[INFO] Loaded module graph from dbpkg with " + modules.size() + " modules");

                // FIX: Build case-insensitive lookup because compiler writes SALES but runtime uses sales
                Map<String, Map<String, Object>> modulesLower = modules.entrySet().stream()
                   .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

                // FORCE ADD RUNTIME - compiler doesn't emit system modules
                modulesLower.putIfAbsent("runtime", Map.of("dependsOn", List.of(), "enabled", true));

                for (String reqMod : requestedModules) {
                    String key = reqMod.toLowerCase(); // sales, inventory, runtime
                    Map<String, Object> modData = modulesLower.get(key);
                    if (modData!= null) {
                        List<String> depsList = (List<String>) modData.getOrDefault("dependsOn", List.of());
                        Set<String> deps = depsList.stream().map(String::toLowerCase).collect(Collectors.toSet());
                        defs.put(reqMod, new ModuleGraph.ModuleDefinition(reqMod, true, deps, Set.of(), Set.of()));
                    } else {
                        System.out.println("[WARN] Module " + reqMod + " not found in module_graph.json");
                    }
                }
            }
        } else {
            System.out.println("[WARN] runtime/module_graph.json not found. Using hardcoded deps");
            defs.put("sales", new ModuleGraph.ModuleDefinition("sales", true, Set.of(), Set.of(), Set.of()));
            defs.put("inventory", new ModuleGraph.ModuleDefinition("inventory", true, Set.of("sales"), Set.of(), Set.of()));
        }

        // Safety: always ensure runtime is in defs
        defs.putIfAbsent("runtime", new ModuleGraph.ModuleDefinition("runtime", true, Set.of(), Set.of(), Set.of()));

        return new ModuleGraph(defs);
    }
}
