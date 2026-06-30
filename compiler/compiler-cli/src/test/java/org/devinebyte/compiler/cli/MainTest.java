package org.devinebyte.compiler.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void shouldInvokeMainWithoutException() {

        assertDoesNotThrow(() ->
                Main.main(new String[]{"help"}));

    }

}
