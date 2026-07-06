package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompilerSDKTest {

    @Test
    void shouldCreateBuilder() {

        CompilerSDKBuilder builder = CompilerSDK.builder();

        assertNotNull(builder);

    }

}
