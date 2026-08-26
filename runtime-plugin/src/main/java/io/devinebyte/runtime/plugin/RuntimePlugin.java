package io.devinebyte.runtime.plugin;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;

public interface RuntimePlugin {
    
    PluginDescriptor descriptor();

    void initialize(PluginContext context, DiagnosticCollector diagnostics);

    void start(DiagnosticCollector diagnostics);

    void stop(DiagnosticCollector diagnostics);

    void shutdown(DiagnosticCollector diagnostics);
}
