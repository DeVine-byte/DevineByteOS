package org.devinebyte.compiler.e2e;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.junit.jupiter.api.Test;

class LogisticsProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileLogisticsBlueprint() {
        CompilerResult result = compile("warehouse");
        CompilationAssertions.succeeded(result.success());
    }
}
