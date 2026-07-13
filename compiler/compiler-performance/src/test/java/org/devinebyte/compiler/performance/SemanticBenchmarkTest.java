package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureSemanticAnalysisPerformance() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.schoolProject());

        assertTrue(result.result().success());
    }
}
