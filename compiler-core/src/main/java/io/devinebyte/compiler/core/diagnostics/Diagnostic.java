package io.devinebyte.compiler.core.diagnostics;

public record Diagnostic(
    DiagnosticSeverity severity,
    String code,
    String message,
    String phase
) {
    public Diagnostic(DiagnosticSeverity severity, String code, String message) {
        this(severity, code, message, null);
    }
}
