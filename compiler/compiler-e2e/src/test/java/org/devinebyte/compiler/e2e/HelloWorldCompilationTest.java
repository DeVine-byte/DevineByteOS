package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.ArtifactAssertions;
import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.CompilerResult;
import org.junit.jupiter.api.Test;

class HelloWorldCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileHelloWorldProject() {
        CompilerResult result = compile("hello-world");

        CompilationAssertions.succeeded(result.success());
        ArtifactAssertions.exists(FixtureManager.outputDirectory());
    }
}
