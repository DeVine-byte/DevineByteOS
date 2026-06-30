package org.devinebyte.compiler.cli.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CompileCommandTest {

    @Test
    void shouldExecuteCompileCommand() {

        CompileCommand command = new CompileCommand();

        assertDoesNotThrow(command::execute);

    }

}
