package com.devinebyte.compiler.cli.commands;

public interface Command {

    /**
     * Executes the command.
     *
     * @return process exit code
     */
    int execute();

}
