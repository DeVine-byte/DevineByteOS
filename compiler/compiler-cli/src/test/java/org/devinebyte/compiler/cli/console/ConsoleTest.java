package org.devinebyte.compiler.cli.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConsoleTest {

    @Test
    void shouldCreateAnsiConsole() {

        Console console = new AnsiConsole();

        assertNotNull(console);

    }

}
