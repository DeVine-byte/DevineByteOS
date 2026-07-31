package io.devinebyte.compiler.sdk.api;

import io.devinebyte.compiler.core.diagnostics.Diagnostic;
import java.nio.file.Path;
import java.util.List;

public record CompilationResult(
    boolean success,
    Path outputDir,
    List<Diagnostic> diagnostics
) {}
