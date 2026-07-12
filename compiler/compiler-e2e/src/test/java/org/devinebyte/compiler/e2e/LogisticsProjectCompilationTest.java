package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class LogisticsProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileLogisticsProject() {

        Path project = FixtureManager.project("warehouse");

        Result result = compile(project);

        CompilationAssertions.assertSuccessful(result);
    }
}
