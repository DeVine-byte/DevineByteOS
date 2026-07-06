package org.devinebyte.compiler.performance;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullCompilationBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldBenchmarkFullCompilation() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.enterpriseProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.result().success());
    }
}
