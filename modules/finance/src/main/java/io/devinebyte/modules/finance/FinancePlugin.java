package io.devinebyte.modules.finance;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.plugin.*;

/**
 * Industry-blind Finance Plugin descriptor.
 */
public class FinancePlugin implements RuntimePlugin {
    private PluginContext ctx;

    @Override
    public PluginDescriptor descriptor() {
        return new PluginDescriptor("finance", "1.0.0", "io.devinebyte.modules.finance.FinancePlugin", "1.0.0", "Finance");
    }

    @Override
    public void initialize(PluginContext context, DiagnosticCollector d) {
        this.ctx = context;
        ctx.logger().info("[FINANCE] Initialized cleanly under industry-blind specifications.", "finance");
    }

    @Override public void start(DiagnosticCollector d) {}
    @Override public void stop(DiagnosticCollector d) {}
    @Override public void shutdown(DiagnosticCollector d) {}
}

