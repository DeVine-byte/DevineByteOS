package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class InvalidProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldFailToCompileInvalidProject() {

        Path project = FixtureManager.project("invalid-project");

        Result result = compile(project);

        CompilationAssertions.assertFailed(result);
    }
}
