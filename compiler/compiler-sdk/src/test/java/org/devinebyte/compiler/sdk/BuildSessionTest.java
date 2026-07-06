package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildSessionTest {

    @Test
    void shouldCreateSession() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        BuildSession session =
                sdk.createSession();

        assertNotNull(session);

    }

}
