package org.devinebyte.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompilerResultTest {

    @Test
    void shouldContainSuccessState() {

        CompilerResult result =
                CompilerResult.success();

        assertTrue(result.success());

    }

}
