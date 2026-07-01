package org.devinebyte.sdk;

import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.Severity;
import java.nio.file.Path;
import java.util.List;

public record CompilerResult(
    List<Path> artifacts,
    List<Diagnostic> diagnostics
) {
    public boolean success() {
        return diagnostics.stream().noneMatch(d -> d.severity() == Severity.ERROR);
    }

    public static CompilerResult failed(List<Diagnostic> diagnostics) {
        return new CompilerResult(List.of(), diagnostics);
    }
}
