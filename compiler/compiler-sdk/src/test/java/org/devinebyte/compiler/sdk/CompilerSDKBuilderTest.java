package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompilerSDKBuilderTest {

    @Test
    void shouldBuildSdk() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        assertNotNull(sdk);

    }

}
