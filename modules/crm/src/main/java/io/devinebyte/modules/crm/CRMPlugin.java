package io.devinebyte.modules.crm;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.plugin.*;

public class CRMPlugin implements RuntimePlugin {
    private PluginContext ctx;

    @Override
    public PluginDescriptor descriptor() {
        return new PluginDescriptor("crm", "1.0.0", "io.devinebyte.modules.crm.CRMPlugin", "1.0.0", "CRM");
    }

    @Override
    public void initialize(PluginContext context, DiagnosticCollector d) {
        this.ctx = context;
        ctx.logger().info("[CRM] Initialized", "crm");
    }

    @Override public void start(DiagnosticCollector d) {}
    @Override public void stop(DiagnosticCollector d) {}
    @Override public void shutdown(DiagnosticCollector d) {}
}

