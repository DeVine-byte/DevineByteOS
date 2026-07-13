package org.devinebyte.compiler.regression.generator;

import org.devinebyte.compiler.regression.CompilerRegressionTestSupport;
import org.devinebyte.compiler.regression.RegressionFixtures;
import org.devinebyte.compiler.testing.assertions.ArtifactAssertions;
import org.junit.jupiter.api.Test;

import static org.devinebyte.compiler.testing.assertions.CompilationAssertions.assertSuccessful;

class REG005RepositoryGenerationTest extends CompilerRegressionTestSupport {

    @Test
    void shouldGenerateRepositoryArtifacts() {
        var output = RegressionFixtures.outputDirectory();
        var result = compile(RegressionFixtures.project("REG-005"), output);
        
        assertSuccessful(result.success());
        ArtifactAssertions.exists(output.resolve("repositories"));
    }
}
