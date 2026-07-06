package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PluginRegistrationTest {

    @Test
    void shouldRegisterPlugin() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        CompilerPlugin plugin = new TestCompilerPlugin();

        assertDoesNotThrow(() ->
                sdk.registerPlugin(plugin));

    }

}
