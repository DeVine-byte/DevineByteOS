package org.devinebyte.compiler.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CliApplicationTest {

    @Test
    void applicationCanBeConstructed() {
        assertDoesNotThrow(CliApplication::new);
    }

    @Test
    void runDoesNotThrowForHelpCommand() {
        CliApplication application = new CliApplication();

        assertDoesNotThrow(() ->
                application.run(new String[]{"help"})
        );
    }

    @Test
    void runDoesNotThrowForVersionCommand() {
        CliApplication application = new CliApplication();

        assertDoesNotThrow(() ->
                application.run(new String[]{"version"})
        );
    }
}
