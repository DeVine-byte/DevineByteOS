package org.devinebyte.compiler.testing.assertions;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import static org.assertj.core.api.Assertions.assertThat;

import org.devinebyte.compiler.sdk.diagnostics.Diagnostic;

public final class DiagnosticAssertions {

    private DiagnosticAssertions() {
    }

    public static void hasNoErrors(
            Iterable<Diagnostic> diagnostics) {

        assertThat(diagnostics)
                .noneMatch(d ->
                        d.getSeverity().name().equals("ERROR"));

    }

}
