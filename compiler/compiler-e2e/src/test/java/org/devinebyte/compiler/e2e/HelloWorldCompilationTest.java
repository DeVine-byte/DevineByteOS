package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.ArtifactAssertions;
import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.Result;
import org.junit.jupiter.api.Test;

class HelloWorldCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileHelloWorldProject() {

        Result result =
                compile(FixtureManager.project("hello-world"));

        CompilationAssertions.assertSuccessful(result);

        ArtifactAssertions.assertExists(
                FixtureManager.outputDirectory()
        );
    }
}
