package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class SchoolProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileSchoolProject() {

        Path project = FixtureManager.project("school");

        Result result = compile(project);

        CompilationAssertions.assertSuccessful(result);
    }
}
