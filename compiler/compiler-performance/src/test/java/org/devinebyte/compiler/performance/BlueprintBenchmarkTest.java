package org.devinebyte.compiler.performance;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureBlueprintCompilationPerformance() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.mediumProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.result().success());
    }
}
