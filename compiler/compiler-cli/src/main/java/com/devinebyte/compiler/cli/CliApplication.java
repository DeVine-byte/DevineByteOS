package com.devinebyte.compiler.cli;

import com.devinebyte.compiler.cli.commands.BuildCommand;
import com.devinebyte.compiler.cli.commands.Command;
import com.devinebyte.compiler.cli.commands.CompileCommand;
import com.devinebyte.compiler.cli.commands.HelpCommand;
import com.devinebyte.compiler.cli.commands.ParseCommand;
import com.devinebyte.compiler.cli.commands.ValidateCommand;
import com.devinebyte.compiler.cli.commands.VersionCommand;
import com.devinebyte.compiler.cli.util.ExitCodes;

public final class CliApplication {

    private CliApplication() {
        throw new AssertionError("Utility class");
    }

    public static int run(String[] args) {

        if (args == null || args.length == 0) {
            return new HelpCommand().execute();
        }

        Command command = switch (args[0].toLowerCase()) {
            case "compile" -> new CompileCommand();
            case "build" -> new BuildCommand();
            case "parse" -> new ParseCommand();
            case "validate" -> new ValidateCommand();
            case "version" -> new VersionCommand();
            case "help" -> new HelpCommand();
            default -> null;
        };

        if (command == null) {
            System.err.println("Unknown command: " + args[0]);
            return ExitCodes.INVALID_ARGUMENT;
        }

        return command.execute();
    }
}
