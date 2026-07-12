package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class MultiModuleCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileEnterpriseProject() {

        Path project = FixtureManager.project("enterprise");

        Result result = compile(project);

        CompilationAssertions.assertSuccessful(result);
    }
}
