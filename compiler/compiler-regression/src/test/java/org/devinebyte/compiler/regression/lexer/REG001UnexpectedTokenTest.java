package org.devinebyte.compiler.regression.lexer;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.regression.CompilerRegressionTestSupport;
import org.devinebyte.compiler.regression.RegressionFixtures;
import org.junit.jupiter.api.Test;

import static org.devinebyte.compiler.testing.assertions.CompilationAssertions.succeeded;

class REG001UnexpectedTokenTest extends CompilerRegressionTestSupport {

    @Test
    void shouldHandleUnexpectedTokenWithoutFailure() {
        var result = compile(
            RegressionFixtures.project("REG-001"),
            RegressionFixtures.outputDirectory()
        );
        succeeded(result.success());
    }
}
