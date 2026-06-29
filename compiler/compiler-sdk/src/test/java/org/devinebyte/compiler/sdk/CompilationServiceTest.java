package org.devinebyte.compiler.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompilationServiceTest {

    @Test
    void shouldProvideCompilationService() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        assertNotNull(sdk.compilationService());

    }

}
