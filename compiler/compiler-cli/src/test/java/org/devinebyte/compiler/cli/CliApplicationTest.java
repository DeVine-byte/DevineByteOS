package org.devinebyte.compiler.cli;

import org.devinebyte.compiler.cli.options.OptionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CliApplicationTest {

    @Test
    void invalidArgumentsThrowOptionException() {

        CliApplication application = new CliApplication();

        assertThrows(
                OptionException.class,
                () -> application.run(new String[]{"--unknown"})
        );
    }
}
