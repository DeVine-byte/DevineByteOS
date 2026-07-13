package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldBenchmarkIncrementalCompilation() {

        BenchmarkResult result =
                benchmarkIncremental(BenchmarkFixtures.enterpriseProject());

        assertTrue(result.result().success());
    }
}
