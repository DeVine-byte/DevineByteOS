package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureMemoryUsage() {

        Runtime runtime = Runtime.getRuntime();

        runtime.gc();

        long before = runtime.totalMemory() - runtime.freeMemory();

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.enterpriseProject());

        runtime.gc();

        long after = runtime.totalMemory() - runtime.freeMemory();

        assertTrue(result.result().successful());
        assertTrue(after >= before);
    }
}
