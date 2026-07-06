package org.devinebyte.compiler.api.plugin;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public final class CompilerEvent {

    private final EventType type;

    public CompilerEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

}
