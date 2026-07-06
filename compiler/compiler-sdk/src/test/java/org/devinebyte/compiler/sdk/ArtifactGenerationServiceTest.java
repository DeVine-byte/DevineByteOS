package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArtifactGenerationServiceTest {

    @Test
    void shouldProvideArtifactGenerationService() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        assertNotNull(sdk.artifactGenerationService());

    }

}
