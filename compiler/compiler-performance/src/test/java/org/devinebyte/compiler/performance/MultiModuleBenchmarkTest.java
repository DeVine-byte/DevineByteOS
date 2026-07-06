package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiModuleBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldBenchmarkMultiModuleCompilation() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.multiModuleProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.result().success());
    }
}
