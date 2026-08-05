package io.devinebyte.runtime;

import io.devinebyte.runtime.bootstrap.DbpkgVerifier;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.bootstrap.RuntimeBootstrapper;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.runtime.core.workflow.WorkflowInstanceReader;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.tenant.TenantRuntime;
import io.devinebyte.runtime.tenant.TenantRuntimeFactory;

import io.devinebyte.runtime.event.core.SimpleEventBus;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.EventMetadata;
import io.devinebyte.runtime.event.storage.FileEventStore;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.core.EventDispatcher;
import io.devinebyte.runtime.event.core.CompositeEventDispatcher;
import io.devinebyte.runtime.event.handler.HandlerRegistry;

import io.devinebyte.runtime.workflow.RuntimeWorkflowModule;
import io.devinebyte.runtime.workflow.engine.WorkflowEventDispatcher;

import io.devinebyte.runtime.projection.RuntimeProjectionModule;
import io.devinebyte.runtime.projection.loader.ProjectionLoadResult;
import io.devinebyte.runtime.projection.store.WorkflowInstanceStore;

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

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 4 ||!args[0].equals("run")) {
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

            System.out.println("Diagnostics: " + bootstrap.diagnostics().getAll());
            if (bootstrap.diagnostics().hasFatal()) {
                System.err.println("Bootstrap failed: " + bootstrap.diagnostics().getAll());
                System.exit(1);
            }

            TenantRuntime runtime = factory.create(bootstrap.tenantContext(), bootstrap);

            Path tenantBase = Path.of("/data/data/com.termux/files/home/DevineByteOS/data/tenants");
            Files.createDirectories(tenantBase);

            moduleRegistry.register(runtime.tenantContext(), runtime.moduleGraph().modules());

            EventDiagnostics eventDiagnostics = new EventDiagnostics(diagnostics);
            EventStore store = new FileEventStore(tenantBase, mapper, eventDiagnostics);
            ModuleIsolationGuard guard = new ModuleIsolationGuard(moduleRegistry);

            // 1. Base dispatcher
            HandlerRegistry handlerRegistry = new HandlerRegistry();
            EventDispatcher baseDispatcher = new EventDispatcher(handlerRegistry, guard);

            // 2. Workflow + Projection need to be in scope for composite
            WorkflowEventDispatcher workflowDispatcher = null;
            RuntimeProjectionModule projectionModule = null;
            ProjectionLoadResult projectionLoad = null;

            // 2a. Workflow
            if (runtime.tenantContext().enabledModules().contains("workflow")) {
                RuntimeWorkflowModule workflowModule = RuntimeWorkflowModule.create(store, guard);
                var workflowLoad = workflowModule.loader().load(fs, runtime.tenantContext(), diagnostics);

                boolean hasWorkflowErrors = workflowLoad.diagnostics().getAll().stream()
               .anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR || d.severity() == DiagnosticSeverity.FATAL);

                if (hasWorkflowErrors) {
                    System.err.println("Workflow load failed: " + workflowLoad.diagnostics().getAll());
                } else {
                    workflowModule.engine().register(workflowLoad.workflows());
                    System.out.println("✅ WorkflowEngine loaded: " + workflowLoad.workflows().keySet());

                    WorkflowInstanceReader reader = new WorkflowInstanceStore(store); // FROM PROJECTION
                    workflowDispatcher = new WorkflowEventDispatcher(workflowModule.engine(), reader);
                }
            }

            // 2b. Projection
            if (runtime.tenantContext().enabledModules().contains("projection")) {
                projectionModule = RuntimeProjectionModule.create(store, guard, diagnostics, mapper);
                projectionLoad = projectionModule.loader().load(fs, diagnostics);

                boolean hasProjectionErrors = projectionLoad.diagnostics().getAll().stream()
               .anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR || d.severity() == DiagnosticSeverity.FATAL);

                if (hasProjectionErrors) {
                    System.err.println("Projection load failed: " + projectionLoad.diagnostics().getAll());
                } else {
                    byte[] wasmBytes = Files.readAllBytes(fs.getPath("projections/projection_functions.wasm"));
                    projectionModule.stateStore().setWasmBytes(wasmBytes);
                    projectionModule.register(projectionLoad);
                    System.out.println("✅ ProjectionEngine loaded: " + projectionLoad.functions().size() + " functions");
                }
            }

            // 3. Composite dispatcher: fanout to all
            List<EventDispatcher> dispatchers = new ArrayList<>();
            dispatchers.add(baseDispatcher);
            if (workflowDispatcher!= null) dispatchers.add(workflowDispatcher);
            if (projectionModule!= null) dispatchers.add(projectionModule.handler().asDispatcher());

            CompositeEventDispatcher dispatcher = new CompositeEventDispatcher(dispatchers);
            SimpleEventBus eventBus = new SimpleEventBus(store, dispatcher, guard);

            // 4. Publish boot event AFTER all handlers are registered
            ObjectNode payload = mapper.createObjectNode().put("boot", "ok").put("ts", System.currentTimeMillis());
            EventMetadata meta = new EventMetadata(
                UUID.randomUUID(), null, "runtime", Instant.now(),
                Map.of("tenantId", runtime.tenantContext().tenantId())
            );
            DomainEvent bootEvent = new DomainEvent("SystemBooted", "1.0", payload, meta);

            eventBus.publish(runtime.tenantContext(), bootEvent);

            System.out.println("✅ Booted tenant: " + runtime.tenantContext().tenantId());
            System.out.println(" From dbpkg: " + runtime.dbpkgPath());
            System.out.println(" Lifecycle: " + runtime.tenantContext().state());
            System.out.println(" Enabled Modules: " + runtime.tenantContext().enabledModules());
            System.out.println(" EventBus: Wrote 1 event to " + tenantBase.resolve(runtime.tenantContext().tenantId()) + "/events.log");
        }
    }

    private static Set<String> readRequestedModules(FileSystem fs, ObjectMapper mapper) throws Exception {
        Path cfg = fs.getPath("runtime/tenant_config.json");
        if (Files.exists(cfg)) {
            try (InputStream in = Files.newInputStream(cfg)) {
                Map<String, Object> json = mapper.readValue(in, Map.class);
                List<String> list = (List<String>) json.getOrDefault("requestedModules", List.of());
                if (!list.isEmpty()) return new HashSet<>(list);
            }
        }
        Path graphPath = fs.getPath("runtime/module_graph.json");
        if (!Files.exists(graphPath)) return Set.of();
        try (InputStream in = Files.newInputStream(graphPath)) {
            Map<String, Object> graph = mapper.readValue(in, Map.class);
            Map<String, Object> modules = (Map<String, Object>) graph.get("modules");
            Set<String> enabled = new HashSet<>();
            if (modules!= null) {
                for (Map.Entry<String, Object> e : modules.entrySet()) {
                    Map<String, Object> def = (Map<String, Object>) e.getValue();
                    if (Boolean.TRUE.equals(def.get("enabled"))) enabled.add(e.getKey());
                }
            }
            return enabled;
        }
    }
}
