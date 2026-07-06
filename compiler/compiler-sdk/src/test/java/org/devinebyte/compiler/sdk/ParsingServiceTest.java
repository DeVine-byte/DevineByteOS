package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParsingServiceTest {

    @Test
    void shouldProvideParsingService() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        assertNotNull(sdk.parsingService());

    }

}
