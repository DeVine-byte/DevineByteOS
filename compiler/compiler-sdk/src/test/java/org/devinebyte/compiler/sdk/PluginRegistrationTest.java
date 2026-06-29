package org.devinebyte.compiler.sdk;

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
