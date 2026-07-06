package org.devinebyte.compiler.cli.commands;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.util.ExitCodes;

public final class HelpCommand implements Command {

    @Override
    public int execute() {

        System.out.println("""
                DevineByte Compiler

                Usage:
                  db <command>

                Commands:
                  compile    Compile source files
                  build      Build a project
                  parse      Parse source files
                  validate   Validate source files
                  version    Display compiler version
                  help       Display this help
                """);

        return ExitCodes.SUCCESS;
    }

}
