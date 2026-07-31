package io.devinebyte.runtime.config;

import io.devinebyte.runtime.core.diagnostics.Diagnostic;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import java.time.Instant;

public final class ConfigLoadException extends RuntimeException {
    private final Diagnostic diagnostic;
    public ConfigLoadException(String code, String message) {
        super(message);
        this.diagnostic = new Diagnostic(code, DiagnosticSeverity.FATAL, message, "", Instant.now());
    }
    public Diagnostic diagnostic() { return diagnostic; }
}
