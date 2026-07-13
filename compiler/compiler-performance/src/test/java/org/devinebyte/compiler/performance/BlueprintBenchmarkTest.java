package org.devinebyte.compiler.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintBenchmarkTest extends BenchmarkSupport {

    @Test
    void shouldMeasureBlueprintCompilationPerformance() {

        BenchmarkResult result =
                benchmark(BenchmarkFixtures.clinicProject());

        assertTrue(result.result().successful());
    }
}
