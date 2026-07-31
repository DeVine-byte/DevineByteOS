package io.devinebyte.runtime.core.diagnostics;

import java.time.Instant;

/**
 * Structured diagnostic. No throwing exceptions for business errors.
 */
public record Diagnostic(
    String code, // Format: DBRT001
    DiagnosticSeverity severity,
    String message,
    String tenantId,
    Instant timestamp
) {
    public Diagnostic {
        timestamp = Instant.now();
    }
}
