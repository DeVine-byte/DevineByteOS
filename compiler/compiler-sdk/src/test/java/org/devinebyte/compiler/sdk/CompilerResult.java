package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.Severity;
import java.nio.file.Path;
import java.util.List;

public record CompilerResult(
    List<Path> artifacts,
    List<Diagnostic> diagnostics
) {
    public boolean success() {
        return diagnostics.stream().noneMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }

    public static CompilerResult failed(List<Diagnostic> diagnostics) {
        return new CompilerResult(List.of(), diagnostics);
    }
}
