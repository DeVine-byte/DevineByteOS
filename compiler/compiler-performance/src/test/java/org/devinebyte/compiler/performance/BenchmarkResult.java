package org.devinebyte.compiler.performance;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.CompilerResult;
import java.time.Duration;

public record BenchmarkResult(
        CompilerResult result,
        Duration duration
) {
}
