package org.devinebyte.compiler.regression.blueprint;

import org.devinebyte.compiler.regression.CompilerRegressionTestSupport;
import org.devinebyte.compiler.regression.RegressionFixtures;
import org.junit.jupiter.api.Test;

import static org.devinebyte.compiler.testing.assertions.CompilationAssertions.succeeded;

class REG004BlueprintValidationTest extends CompilerRegressionTestSupport {

    @Test
    void shouldCompileBlueprintSuccessfully() {
        var result = compile(
            RegressionFixtures.project("REG-004"),
            RegressionFixtures.outputDirectory()
        );
        succeeded(result.success());
    }
}
