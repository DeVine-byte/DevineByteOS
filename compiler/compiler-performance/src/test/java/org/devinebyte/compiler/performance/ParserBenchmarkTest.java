package org.devinebyte.compiler.performance;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserBenchmarkTest extends CompilerBenchmarkSupport {

    @Test
    void shouldMeasureParserPerformance() {
        BenchmarkResult r = benchmark(BenchmarkFixtures.mediumProject(), BenchmarkFixtures.outputDirectory());
        assertTrue(r.duration().toMillis() >= 0);
        assertTrue(r.result().success());
    }
}
