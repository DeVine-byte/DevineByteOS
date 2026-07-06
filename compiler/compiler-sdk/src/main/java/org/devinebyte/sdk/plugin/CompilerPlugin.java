package org.devinebyte.sdk.plugin;
import org.devinebyte.compiler.api.DiagnosticSeverity;

public interface CompilerPlugin {

    String getId();

    String getName();

    String getVersion();

    void initialize(PluginContext context);

}
