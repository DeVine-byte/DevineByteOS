package org.devinebyte.sdk.plugin;
import org.devinebyte.compiler.api.DiagnosticSeverity;

public interface EventListener {

    void onEvent(CompilerEvent event);

}
