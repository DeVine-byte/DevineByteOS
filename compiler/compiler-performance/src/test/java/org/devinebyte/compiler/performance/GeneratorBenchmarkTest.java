package org.devinebyte.compiler.performance;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureCodeGenerationPerformance() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.largeProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.result().success());
    }
}
