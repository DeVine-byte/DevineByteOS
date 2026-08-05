package io.devinebyte.runtime.projection.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kawamuray.wasmtime.*;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity; // NEW
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.projection.model.ProjectionResult;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class WasmRuntime implements AutoCloseable {
    private final Engine engine;
    private final ObjectMapper mapper;

    @Inject
    public WasmRuntime(ObjectMapper mapper) {
        this.engine = new Engine();
        this.mapper = mapper;
    }

    public JsonNode invoke(TenantContext tenant, ProjectionContext ctx, ProjectionFunction fn, DomainEvent event) {
        try (Store<Void> store = Store.withoutData(engine)) {
            Func logFn = Func.wrap(store, (Caller caller, String msg) ->
                System.out.println("[PROJECTION][" + tenant.tenantId() + "] " + msg));

            byte[] wasmBytes = ctx.stateStore().loadWasmBytes();
            if (wasmBytes == null) throw new IllegalStateException("WASM bytes not loaded");

            Module module = Module.fromBinary(engine, wasmBytes);
            Linker linker = new Linker(engine);
            linker.define("env", "log", logFn);
            Instance instance = linker.instantiate(store, module);
            Func target = instance.getFunc(store, fn.wasmExport())
                .orElseThrow(() -> new IllegalStateException("Export not found: " + fn.wasmExport()));

            String eventJson = mapper.writeValueAsString(event);
            target.call(store, eventJson); // we ignore return ptr for v1

            return mapper.createObjectNode().put("ok", true);
        } catch (Exception e) {
            ctx.diagnostics().add("DBRT601", DiagnosticSeverity.ERROR, "Wasm invoke failed: " + e.getMessage());
            return null;
        }
    }

    @Override public void close() { engine.close(); }
}
