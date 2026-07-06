package org.devinebyte.sdk.plugin;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public final class CompilerEvent {

    private final EventType type;

    public CompilerEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

}
