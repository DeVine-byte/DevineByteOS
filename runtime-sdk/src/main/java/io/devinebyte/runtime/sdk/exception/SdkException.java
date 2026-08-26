package io.devinebyte.runtime.sdk.exception;

import io.devinebyte.runtime.core.diagnostics.Diagnostic;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import java.time.Instant;

public class SdkException extends RuntimeException {
    private final String code;

    public SdkException(String code, String message) {
        super(message);
        this.code = code;
    }

    public Diagnostic toDiagnostic() {
        return new Diagnostic(code, DiagnosticSeverity.ERROR, getMessage(), null, Instant.now());
    }
}
