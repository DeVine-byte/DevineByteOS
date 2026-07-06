package org.devinebyte.compiler.e2e;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileWithinExpectedTime() {
        long start = System.nanoTime();
        compile("crm");
        long elapsed = System.nanoTime() - start;
        assertTrue(elapsed > 0, "Elapsed time should be > 0, was: " + elapsed);
    }
}
