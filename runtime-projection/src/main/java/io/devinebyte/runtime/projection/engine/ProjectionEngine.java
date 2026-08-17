package io.devinebyte.runtime.projection.engine;

import com.fasterxml.jackson.databind.JsonNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.projection.diagnostics.ProjectionDiagnostics;
import io.devinebyte.runtime.projection.model.ProjectionResult;
import io.devinebyte.runtime.projection.store.ProjectionStateStore;
import io.devinebyte.compiler.projection.model.ProjectionFunction;

@jakarta.inject.Singleton
public final class ProjectionEngine {
    private final WasmRuntime wasm;
    private final ProjectionStateStore store;
    private final ModuleIsolationGuard guard;
    private final ProjectionDiagnostics diagnostics;
    private static final String DEFAULT_MODULE = "Sales"; // v1: infer from name later

    @jakarta.inject.Inject
    public ProjectionEngine(WasmRuntime wasm, ProjectionStateStore store, ModuleIsolationGuard guard, ProjectionDiagnostics diagnostics) {
        this.wasm = wasm; this.store = store; this.guard = guard; this.diagnostics = diagnostics;
    }

    public ProjectionResult execute(TenantContext tenant, ProjectionFunction function, DomainEvent event) {
        String moduleId = DEFAULT_MODULE; // TODO: derive from function.name()
        try {
            guard.assertEnabled(tenant, moduleId, "projection:" + function.name());
        } catch (Exception e) {
            diagnostics.moduleDisabled(tenant.tenantId(), moduleId);
            return null;
        }
        ProjectionContext ctx = new ProjectionContext(tenant, event, store, guard, diagnostics.collector());
        JsonNode output = wasm.invoke(tenant, ctx, function, event);
        store.save(tenant, function.name(), output);
        return new ProjectionResult(function.name(), output, event.occurredAt());
    }
}
