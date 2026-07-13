package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThroughputBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureCompilationThroughput() {

        long start = System.nanoTime();

        for (int i = 0; i < 10; i++) {
            BenchmarkResult result =
                    benchmark(BenchmarkFixtures.helloWorldProject());

            assertTrue(result.result().successful());
        }

        long elapsed = System.nanoTime() - start;

        assertTrue(elapsed > 0);
    }
}
