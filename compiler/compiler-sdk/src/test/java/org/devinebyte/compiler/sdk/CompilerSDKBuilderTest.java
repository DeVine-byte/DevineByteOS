package org.devinebyte.compiler.sdk;

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
