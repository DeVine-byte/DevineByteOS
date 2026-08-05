package io.devinebyte.runtime.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.projection.diagnostics.ProjectionDiagnostics;
import io.devinebyte.runtime.projection.engine.ProjectionEngine;
import io.devinebyte.runtime.projection.engine.WasmRuntime;
import io.devinebyte.runtime.projection.handler.ProjectionEventHandler;
import io.devinebyte.runtime.projection.loader.ProjectionLoadResult;
import io.devinebyte.runtime.projection.loader.ProjectionLoader;
import io.devinebyte.runtime.projection.store.FileProjectionStateStore;
import jakarta.inject.Singleton;

@Singleton
public final class RuntimeProjectionModule {
    private final ProjectionLoader loader;
    private final ProjectionEngine engine;
    private final ProjectionEventHandler handler;
    private final FileProjectionStateStore stateStore;

    private RuntimeProjectionModule(
        ProjectionLoader loader,
        ProjectionEngine engine,
        ProjectionEventHandler handler,
        FileProjectionStateStore stateStore
    ) {
        this.loader = loader;
        this.engine = engine;
        this.handler = handler;
        this.stateStore = stateStore;
    }

    // Factory for Main.java - avoids needing DI to construct 6 things
    public static RuntimeProjectionModule create(
        EventStore eventStore,
        ModuleIsolationGuard guard,
        DiagnosticCollector diagnostics,
        ObjectMapper mapper
    ) {
        var loader = new ProjectionLoader(mapper);
        var stateStore = new FileProjectionStateStore();
        var wasmRuntime = new WasmRuntime(mapper);
        var projDiagnostics = new ProjectionDiagnostics();
        var engine = new ProjectionEngine(wasmRuntime, stateStore, guard, projDiagnostics);
        var handler = new ProjectionEventHandler(engine, projDiagnostics); // loadResult set later via register()
        return new RuntimeProjectionModule(loader, engine, handler, stateStore);
    }

    // Called by Main after loader.load() succeeds
    public void register(ProjectionLoadResult loadResult) {
        this.handler.setLoadResult(loadResult);
        // handler already implements EventHandler, so Main subscribes it via dispatcher
    }

    public ProjectionLoader loader() { return loader; }
    public ProjectionEngine engine() { return engine; }
    public ProjectionEventHandler handler() { return handler; }
    public FileProjectionStateStore stateStore() { return stateStore; }
}
