package org.devinebyte.compiler.cli;

import org.devinebyte.compiler.cli.util.ExitCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CliApplicationTest {

    @Test
    void shouldReturnSuccessForHelpCommand() {

        int exitCode = CliApplication.run(new String[]{"help"});

        assertEquals(ExitCodes.SUCCESS, exitCode);

    }

    @Test
    void shouldReturnInvalidArgumentForUnknownCommand() {

        int exitCode = CliApplication.run(new String[]{"unknown"});

        assertEquals(ExitCodes.INVALID_ARGUMENT, exitCode);

    }

}
