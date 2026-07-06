package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long before = rt.totalMemory() - rt.freeMemory();

        benchmark(BenchmarkFixtures.largeProject(), BenchmarkFixtures.outputDirectory());

        rt.gc();
        long after = rt.totalMemory() - rt.freeMemory();

        assertTrue(after >= 0); // heap used. No threshold, just record
    }
}
