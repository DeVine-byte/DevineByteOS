package org.devinebyte.compiler.performance;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThroughputBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureCompilationThroughput() {
        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            benchmark(BenchmarkFixtures.smallProject(), BenchmarkFixtures.outputDirectory());
        }
        long elapsed = System.nanoTime() - start;
        assertTrue(elapsed > 0);
    }
}
