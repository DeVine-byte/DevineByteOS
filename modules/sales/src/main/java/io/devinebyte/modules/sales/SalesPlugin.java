package io.devinebyte.modules.sales;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.plugin.*;

/**
 * Industry-blind Sales Plugin descriptor.
 * Core event routing and state transitions are handled dynamically by the engine.
 */
public class SalesPlugin implements RuntimePlugin {
    private PluginContext ctx;

    @Override
    public PluginDescriptor descriptor() {
        return new PluginDescriptor("sales", "1.0.0", "io.devinebyte.modules.sales.SalesPlugin", "1.0.0", "Sales");
    }

    @Override
    public void initialize(PluginContext context, DiagnosticCollector d) {
        this.ctx = context;
        ctx.logger().info("[SALES] Initialized cleanly under industry-blind specifications.", "sales");
    }

    @Override public void start(DiagnosticCollector d) {}
    @Override public void stop(DiagnosticCollector d) {}
    @Override public void shutdown(DiagnosticCollector d) {}
}

