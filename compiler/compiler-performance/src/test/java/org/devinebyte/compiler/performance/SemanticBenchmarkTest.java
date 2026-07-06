package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureSemanticAnalysisPerformance() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.largeProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.result().success());
    }
}
