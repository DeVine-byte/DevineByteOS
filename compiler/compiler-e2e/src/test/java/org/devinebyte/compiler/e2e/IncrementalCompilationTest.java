package org.devinebyte.compiler.e2e;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.junit.jupiter.api.Test;

class IncrementalCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldSupportIncrementalCompilation() {
        compile("crm"); // first pass
        CompilerResult second = compileIncremental("crm");
        CompilationAssertions.succeeded(second.success());
    }
}
