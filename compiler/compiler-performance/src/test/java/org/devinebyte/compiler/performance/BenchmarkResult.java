package org.devinebyte.compiler.performance;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.sdk.CompilerResult;
import java.time.Duration;

public record BenchmarkResult(
        CompilerResult result,
        Duration duration
) {
}
