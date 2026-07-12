package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class PerformanceCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileProjectWithinPerformanceBudget() {

        Path project = FixtureManager.project("crm");

        long start = System.nanoTime();

        Result result = compile(project);

        long durationMillis =
                (System.nanoTime() - start) / 1_000_000;

        CompilationAssertions.assertSuccessful(result);

        // Temporary performance budget.
        // Tighten this threshold once the compiler pipeline is fully implemented.
        org.junit.jupiter.api.Assertions.assertTrue(
                durationMillis < 5_000,
                "Compilation took " + durationMillis + " ms"
        );
    }
}
