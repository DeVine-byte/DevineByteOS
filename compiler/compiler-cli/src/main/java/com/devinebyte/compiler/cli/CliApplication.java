package com.devinebyte.compiler.cli;

import com.devinebyte.compiler.cli.util.ExitCodes;

public final class CliApplication {

    private CliApplication() {
        throw new AssertionError("Utility class");
    }

    public static int run(String[] args) {

        if (args == null || args.length == 0) {
            System.out.println("DevineByte Compiler CLI");
            System.out.println("Use 'db help' for available commands.");
            return ExitCodes.SUCCESS;
        }

        String command = args[0];

        switch (command.toLowerCase()) {

            case "help":
                System.out.println("Help command is not implemented yet.");
                return ExitCodes.SUCCESS;

            case "version":
                System.out.println("DevineByte Compiler");
                return ExitCodes.SUCCESS;

            default:
                System.err.println("Unknown command: " + command);
                return ExitCodes.INVALID_ARGUMENT;
        }

    }

}
