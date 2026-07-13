package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Measures lexer performance using the smallest fixture.
 */
class LexerBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureLexerPerformance() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.helloWorldProject());

        assertTrue(result.result().success());
        assertTrue(result.nanoseconds() > 0);
    }
}
