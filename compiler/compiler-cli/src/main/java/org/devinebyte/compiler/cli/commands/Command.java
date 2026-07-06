package org.devinebyte.compiler.cli.commands;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public interface Command {

    /**
     * Executes the command.
     *
     * @return process exit code
     */
    int execute();

}
