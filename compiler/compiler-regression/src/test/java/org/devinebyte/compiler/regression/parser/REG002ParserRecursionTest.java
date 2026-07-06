package org.devinebyte.compiler.regression.parser;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.regression.CompilerRegressionTestSupport;
import org.devinebyte.compiler.regression.RegressionFixtures;
import org.junit.jupiter.api.Test;

import static org.devinebyte.compiler.testing.assertions.CompilationAssertions.succeeded;

class REG002ParserRecursionTest extends CompilerRegressionTestSupport {

    @Test
    void shouldNotEnterInfiniteRecursion() {
        var result = compile(
            RegressionFixtures.project("REG-002"),
            RegressionFixtures.outputDirectory()
        );
        succeeded(result.success());
    }
}
