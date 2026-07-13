package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FullCompilationBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldBenchmarkFullCompilation() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.enterpriseProject());

        assertTrue(result.result().successful());
    }
}
