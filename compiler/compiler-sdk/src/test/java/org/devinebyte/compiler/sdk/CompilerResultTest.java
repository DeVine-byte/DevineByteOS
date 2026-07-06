package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.Severity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompilerResultTest {

    @Test
    void shouldBeSuccessWhenNoErrors() {
        var result = new CompilerResult(
            List.of(Path.of("out/User.java")),
            List.of(new Diagnostic(DiagnosticSeverity.INFO, "INF001", "Done", null))
        );
        assertTrue(result.success());
    }

    @Test
    void shouldBeFailureWhenErrorPresent() {
        var result = new CompilerResult(
            List.of(),
            List.of(new Diagnostic(DiagnosticSeverity.ERROR, "SEM001", "Duplicate", null))
        );
        assertFalse(result.success());
    }
}
