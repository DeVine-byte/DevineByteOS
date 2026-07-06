package org.devinebyte.compiler.e2e;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.junit.jupiter.api.Test;

class MultiModuleCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileMultiModuleProject() {
        CompilerResult result = compile("enterprise");
        CompilationAssertions.succeeded(result.success());
    }
}
