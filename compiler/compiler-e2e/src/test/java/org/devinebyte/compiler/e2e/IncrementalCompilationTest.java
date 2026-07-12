package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class IncrementalCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileIncrementally() {

        Path project = FixtureManager.project("crm");

        Result first = compile(project);
        CompilationAssertions.assertSuccessful(first);

        Result second = compileIncremental(project);
        CompilationAssertions.assertSuccessful(second);
    }
}
