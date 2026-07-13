package org.devinebyte.compiler.regression.semantic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.regression.CompilerRegressionTestSupport;
import org.devinebyte.compiler.regression.RegressionFixtures;
import org.junit.jupiter.api.Test;

import static org.devinebyte.compiler.testing.assertions.CompilationAssertions.assertFailed;

class REG003DuplicateEntityTest extends CompilerRegressionTestSupport {

    @Test
    void shouldRejectDuplicateEntities() {
        var result = compile(
            RegressionFixtures.project("REG-003"),
            RegressionFixtures.outputDirectory()
        );
        assertFailed(result.success());
    }
}
