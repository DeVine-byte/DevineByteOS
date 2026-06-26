package com.devinebyte.compiler.cli.commands;

import com.devinebyte.compiler.cli.util.ExitCodes;

public final class VersionCommand implements Command {

    private static final String VERSION = "0.1.0";

    @Override
    public int execute() {

        System.out.println("DevineByte Compiler");
        System.out.println("Version " + VERSION);

        return ExitCodes.SUCCESS;
    }

}
