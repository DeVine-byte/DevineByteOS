package org.devinebyte.compiler.e2e;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.assertions.ArtifactAssertions;
import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class ArtifactGenerationTest extends CompilerE2ETestSupport {

    @Test
    void shouldGenerateArtifacts() {
        compile("clinic");
        Path output = FixtureManager.outputDirectory();
        ArtifactAssertions.exists(output.resolve("domain"));
        ArtifactAssertions.exists(output.resolve("api"));
    }
}
