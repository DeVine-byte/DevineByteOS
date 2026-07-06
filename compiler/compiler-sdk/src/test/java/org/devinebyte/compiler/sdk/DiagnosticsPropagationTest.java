package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DiagnosticsPropagationTest {

    @Test
    void shouldReturnDiagnosticsCollection() {

        CompilerResult result =
                CompilerResult.success();

        assertNotNull(result.diagnostics());

    }

}
