package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public record CompilationResult(
    boolean success,
    String output,
    String error
) {}
