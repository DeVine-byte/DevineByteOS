package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiModuleBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldBenchmarkMultiModuleCompilation() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.multiModuleProject());

        assertTrue(result.result().success());
    }
}
