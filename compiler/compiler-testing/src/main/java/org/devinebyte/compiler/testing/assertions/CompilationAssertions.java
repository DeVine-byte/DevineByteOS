package org.devinebyte.compiler.testing.assertions;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import static org.assertj.core.api.Assertions.assertThat;

public final class CompilationAssertions {

    private CompilationAssertions() {
    }

    public static void succeeded(boolean success) {

        assertThat(success).isTrue();

    }

}
