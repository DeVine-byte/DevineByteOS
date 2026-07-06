package org.devinebyte.compiler.core;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CompilerConfigurationTest {

    @Test
    void shouldStoreConfigurationValues() {

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        "build",
                        false,
                        true
                );

        assertEquals("build", configuration.outputDirectory());

        assertFalse(configuration.strictMode());

        assertEquals(true, configuration.incremental());

    }

}
