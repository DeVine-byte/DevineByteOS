package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemoryBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureMemoryUsage() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.enterpriseProject());

        assertTrue(result.result().success());

        assertTrue(result.milliseconds() >= 0);
    }
}
