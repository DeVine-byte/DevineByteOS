package com.devinebyte.compiler.cli.commands;

import com.devinebyte.compiler.cli.util.ExitCodes;

public final class ValidateCommand implements Command {

    @Override
    public int execute() {

        System.out.println("Validate command");

        return ExitCodes.SUCCESS;
    }

}
