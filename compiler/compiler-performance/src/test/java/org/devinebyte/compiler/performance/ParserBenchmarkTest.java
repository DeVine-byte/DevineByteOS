package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureParserPerformance() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.clinicProject());

        assertTrue(result.result().successful());
        assertTrue(result.milliseconds() >= 0);
    }
}
