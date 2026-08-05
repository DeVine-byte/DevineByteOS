package io.devinebyte.runtime.projection.handler;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventDispatcher; // NEW
import io.devinebyte.runtime.event.handler.EventHandler;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.projection.engine.ProjectionEngine;
import io.devinebyte.runtime.projection.loader.ProjectionLoadResult;
import io.devinebyte.runtime.projection.diagnostics.ProjectionDiagnostics;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public final class ProjectionEventHandler implements EventHandler {
    private final ProjectionEngine engine;
    private final ProjectionDiagnostics diagnostics;
    private ProjectionLoadResult loadResult; // CHANGED: not final, set via setter

    @Inject
    public ProjectionEventHandler(ProjectionEngine engine, ProjectionDiagnostics diagnostics) { // CHANGED: removed loadResult
        this.engine = engine; 
        this.diagnostics = diagnostics;
    }

    public void setLoadResult(ProjectionLoadResult loadResult) { // NEW
        this.loadResult = loadResult;
    }

    @Override
    public void handle(TenantContext tenant, DomainEvent event) {
        if (loadResult == null) return; // safety for boot before register
        List<ProjectionFunction> functions = loadResult.functions().stream()
            .filter(f -> tenant.enabledModules().contains(f.moduleId()))
            .toList();
        for (ProjectionFunction fn : functions) {
            engine.execute(tenant, fn, event); // 1:1 for deterministic replay
        }
    }

    // NEW: Adapter so we can add this to CompositeEventDispatcher
    public EventDispatcher asDispatcher() {
        return this::handle;
    }
}
