package io.devinebyte.runtime.projection.handler;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.handler.EventHandler;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.projection.engine.ProjectionEngine;
import io.devinebyte.runtime.projection.loader.ProjectionLoadResult;
import io.devinebyte.compiler.projection.model.ProjectionFunction;

import java.util.Map;
import java.util.stream.Collectors;

public final class ProjectionEventHandler implements EventHandler {
    private final ProjectionEngine engine;
    private final TenantContext tenant;
    private final Map<String, ProjectionFunction> byEventType;
    private final String primaryEventType; // v1: only handle 1 type per handler instance

    public ProjectionEventHandler(ProjectionEngine engine, TenantContext tenant, ProjectionLoadResult loadResult) {
        this.engine = engine;
        this.tenant = tenant;
        this.byEventType = loadResult.functions().stream()
                .collect(Collectors.toMap(ProjectionFunction::eventType, f -> f));
        this.primaryEventType = loadResult.functions().isEmpty() ? "" : loadResult.functions().get(0).eventType();
    }

    @Override
    public String moduleId() {
        return "projection";
    }

    @Override
    public String eventType() {
        return primaryEventType; // FIX: return the actual event type
    }

    @Override
    public void handle(TenantContext ctx, DomainEvent event) {
        ProjectionFunction fn = byEventType.get(event.type());
        if (fn != null) {
            engine.execute(ctx, fn, event);
        }
    }
}
