package org.devinebyte.compiler.cli.commands;

import org.devinebyte.compiler.cli.util.ExitCodes;

public final class ParseCommand implements Command {

    @Override
    public int execute() {

        System.out.println("Parse command");

        return ExitCodes.SUCCESS;
    }

}
