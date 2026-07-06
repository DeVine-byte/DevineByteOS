package org.devinebyte.compiler.performance;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldBenchmarkIncrementalCompilation() {
        benchmark(BenchmarkFixtures.enterpriseProject(), BenchmarkFixtures.outputDirectory()); // warm
        BenchmarkResult second = benchmarkIncremental(BenchmarkFixtures.enterpriseProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(second.result().success());
    }
}
