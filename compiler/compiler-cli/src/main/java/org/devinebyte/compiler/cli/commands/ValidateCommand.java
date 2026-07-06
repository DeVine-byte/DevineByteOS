package org.devinebyte.compiler.cli.commands;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.util.ExitCodes;

public final class ValidateCommand implements Command {

    @Override
    public int execute() {

        System.out.println("Validate command");

        return ExitCodes.SUCCESS;
    }

}
