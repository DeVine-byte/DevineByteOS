package io.devinebyte.runtime.tenant;

import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;
import io.devinebyte.runtime.bootstrap.BootstrapResult;
import io.devinebyte.runtime.bootstrap.ManifestReader;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.module.ModuleLoader;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.orchestration.RuntimeOrchestrationModule;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public final class TenantRuntime implements AutoCloseable {
    private final TenantContext context;
    private final ManifestReader.Manifest manifest;
    private final Path dbpkgPath;
    private final EventBus eventBus;
    private final EventStore eventStore;
    private final RuntimeOrchestrationModule orchestration;
    private final RuntimeRegistry runtimeRegistry;
    private final ModuleRegistry moduleRegistry;
    private final DiagnosticCollector diagnostics;

    @Inject
    public TenantRuntime(
        TenantContext context,
        ManifestReader.Manifest manifest,
        Path dbpkgPath,
        EventBus eventBus,
        EventStore eventStore,
        RuntimeOrchestrationModule orchestration,
        RuntimeRegistry runtimeRegistry,
        ModuleRegistry moduleRegistry,
        DiagnosticCollector diagnostics
    ) {
        this.context = context;
        this.manifest = manifest;
        this.dbpkgPath = dbpkgPath;
        this.eventBus = eventBus;
        this.eventStore = eventStore;
        this.orchestration = orchestration;
        this.runtimeRegistry = runtimeRegistry;
        this.moduleRegistry = moduleRegistry;
        this.diagnostics = diagnostics;
    }

    public TenantContext context() { return context; }
    public EventBus eventBus() { return eventBus; }
    public EventStore eventStore() { return eventStore; }
    public RuntimeOrchestrationModule orchestration() { return orchestration; }
    public ManifestReader.Manifest manifest() { return manifest; }

    public void boot() {
        diagnostics.add(new io.devinebyte.runtime.core.diagnostics.Diagnostic(
            "BOOT_001", io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity.INFO,
            "TenantRuntime booted. version=" + manifest.version(), context.tenantId(), null
        ));
    }

    @Override
    public void close() {
        diagnostics.add(new io.devinebyte.runtime.core.diagnostics.Diagnostic(
            "BOOT_002", io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity.INFO,
            "TenantRuntime shutdown", context.tenantId(), null
        ));
    }
}
