package org.devinebyte.compiler.e2e;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.junit.jupiter.api.Test;

class SchoolProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileSchoolBlueprint() {
        CompilerResult result = compile("school");
        CompilationAssertions.succeeded(result.success());
    }
}
