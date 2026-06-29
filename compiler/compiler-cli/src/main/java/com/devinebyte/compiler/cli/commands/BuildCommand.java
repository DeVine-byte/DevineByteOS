package org.devinebyte.compiler.cli.commands;

import org.devinebyte.compiler.cli.util.ExitCodes;

public final class BuildCommand implements Command {

    @Override
    public int execute() {

        System.out.println("Build command");

        return ExitCodes.SUCCESS;
    }

}
