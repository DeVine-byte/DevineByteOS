package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompilerSDKTest {

    @Test
    void shouldCreateBuilder() {

        CompilerSDKBuilder builder = CompilerSDK.builder();

        assertNotNull(builder);

    }

}
