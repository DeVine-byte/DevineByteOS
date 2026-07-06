package org.devinebyte.compiler.api;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public record CompilationResult(
    boolean success,
    String output,
    String error
) {}
