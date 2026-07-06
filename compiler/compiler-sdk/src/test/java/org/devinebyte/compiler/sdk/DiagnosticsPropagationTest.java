package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

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
