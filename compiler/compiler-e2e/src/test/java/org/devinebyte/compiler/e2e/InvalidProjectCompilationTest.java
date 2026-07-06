package org.devinebyte.compiler.e2e;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.junit.jupiter.api.Test;

class InvalidProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldRejectInvalidProject() {
        CompilerResult result = compile("invalid-project");
        CompilationAssertions.failed(result.success());
    }
}
