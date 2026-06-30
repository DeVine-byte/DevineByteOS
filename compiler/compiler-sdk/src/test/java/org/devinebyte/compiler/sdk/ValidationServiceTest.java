package org.devinebyte.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidationServiceTest {

    @Test
    void shouldProvideValidationService() {

        CompilerSDK sdk =
                CompilerSDK.builder().build();

        assertNotNull(sdk.validationService());

    }

}
