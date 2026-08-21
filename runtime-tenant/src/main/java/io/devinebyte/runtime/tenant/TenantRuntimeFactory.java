package io.devinebyte.runtime.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;
import io.devinebyte.runtime.bootstrap.BootstrapResult;
import io.devinebyte.runtime.config.ConfigurationManager;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.event.core.EventDispatcher;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.handler.HandlerRegistry;
import io.devinebyte.runtime.event.storage.FileEventStore;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.orchestration.RuntimeOrchestrationModule;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import io.devinebyte.runtime.workflow.engine.WorkflowExecutor;
import io.devinebyte.runtime.workflow.engine.FileWorkflowInstanceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Singleton
public class TenantRuntimeFactory {
    private final ConfigurationManager configManager;
    private final ObjectMapper mapper;
    private final ModuleLoader moduleLoader;
    private final ModuleRegistry moduleRegistry;
    private final RuntimeRegistry runtimeRegistry;

    @Inject
    public TenantRuntimeFactory(
        ConfigurationManager configManager,
        ObjectMapper mapper,
        ModuleLoader moduleLoader,
        ModuleRegistry moduleRegistry,
        RuntimeRegistry runtimeRegistry
    ) {
        this.configManager = configManager;
        this.mapper = mapper;
        this.moduleLoader = moduleLoader;
        this.moduleRegistry = moduleRegistry;
        this.runtimeRegistry = runtimeRegistry;
    }

    public TenantRuntime create(TenantContext tenant, BootstrapResult bootstrap, FileSystem fs, DiagnosticCollector diagnostics) throws Exception {
        Path tenantBase = Path.of("build/data/tenants");
        Files.createDirectories(tenantBase);

        EventDiagnostics eventDiagnostics = new EventDiagnostics(diagnostics);
        EventStore eventStore = new FileEventStore(tenantBase, mapper, eventDiagnostics);
        ModuleIsolationGuard guard = new ModuleIsolationGuard(moduleRegistry);

        HandlerRegistry handlerRegistry = new HandlerRegistry();
        EventDispatcher dispatcher = new EventDispatcher(handlerRegistry, guard);
        EventBus eventBus = new EventBus(eventStore, dispatcher, guard);

        var workflowRepo = new FileWorkflowInstanceRepository(eventStore, tenantBase);
        var workflowExecutor = new WorkflowExecutor(eventStore, guard);
        WorkflowEngine workflowEngine = new WorkflowEngine(workflowRepo, workflowExecutor);

        // Load modules from dbpkg so guard allows events
        Path graphPath = fs.getPath("runtime/module_graph.json");
        ModuleGraph graph = mapper.readValue(Files.newInputStream(graphPath), ModuleGraph.class);
        moduleRegistry.register(tenant, graph.modules());

        RuntimeOrchestrationModule orchestration = RuntimeOrchestrationModule.create(
            eventBus, moduleRegistry, runtimeRegistry, workflowEngine, fs
        );

        // Publish boot event from "runtime" module - this creates events.log
        ObjectNode payload = mapper.createObjectNode();
        payload.put("tenantId", tenant.tenantId());
        payload.put("version", bootstrap.manifest().version());
        payload.put("at", Instant.now().toString());

        DomainEvent bootEvent = DomainEvent.create(tenant, "runtime", "SystemBooted", "1.0", payload);
        eventBus.publish(tenant, bootEvent);

        return new TenantRuntime(
            bootstrap.tenantContext(),
            bootstrap.manifest(),
            bootstrap.dbpkgPath(),
            eventBus,
            eventStore,
            orchestration,
            runtimeRegistry,
            moduleRegistry,
            diagnostics
        );
    }
}
