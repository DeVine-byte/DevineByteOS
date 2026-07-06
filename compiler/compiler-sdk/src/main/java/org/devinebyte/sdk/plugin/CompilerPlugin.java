package org.devinebyte.sdk.plugin;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public interface CompilerPlugin {

    String getId();

    String getName();

    String getVersion();

    void initialize(PluginContext context);

}
