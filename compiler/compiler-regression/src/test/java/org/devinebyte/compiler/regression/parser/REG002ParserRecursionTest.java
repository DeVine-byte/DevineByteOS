package org.devinebyte.compiler.regression.parser;

import org.devinebyte.compiler.regression.CompilerRegressionTestSupport;
import org.devinebyte.compiler.regression.RegressionFixtures;
import org.junit.jupiter.api.Test;

import static org.devinebyte.compiler.testing.assertions.CompilationAssertions.assertSuccessful;

class REG002ParserRecursionTest extends CompilerRegressionTestSupport {

    @Test
    void shouldNotEnterInfiniteRecursion() {
        var result = compile(
            RegressionFixtures.project("REG-002"),
            RegressionFixtures.outputDirectory()
        );
        assertSuccessful(result.success());
    }
}
