package com.devinebyte.compiler.cli.commands;

import com.devinebyte.compiler.cli.util.ExitCodes;

public final class CompileCommand implements Command {

    @Override
    public int execute() {

        System.out.println("Compile command");

        // SDK integration will be added during CLI integration.

        return ExitCodes.SUCCESS;
    }

}
