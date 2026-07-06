package org.devinebyte.compiler.api.plugin;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public interface EventListener {

    void onEvent(CompilerEvent event);

}
