package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureCodeGenerationPerformance() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.warehouseProject());

        assertTrue(result.result().success());
    }
}
