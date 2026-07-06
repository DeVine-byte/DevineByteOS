package org.devinebyte.compiler.api.plugin;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public interface CompilerPlugin {

    String getId();

    String getName();

    String getVersion();

    void initialize(PluginContext context);

}
