package org.devinebyte.compiler.performance;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexerBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureLexerPerformance() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.smallProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.duration().toNanos() > 0);
        assertTrue(r.result().success());
    }
}
