package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class ClinicProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileClinicProject() {

        Path project = FixtureManager.project("clinic");

        Result result = compile(project);

        CompilationAssertions.assertSuccessful(result);
    }
}
