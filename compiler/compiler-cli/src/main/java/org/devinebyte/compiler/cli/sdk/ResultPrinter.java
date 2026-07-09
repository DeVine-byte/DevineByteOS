package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.compiler.cli.console.Console;
import org.devinebyte.sdk.Result;

public final class ResultPrinter {

    private final Console console;

    public ResultPrinter(Console console) {
        this.console = console;
    }

    /**
     * Prints the compilation result to the console.
     */
    public void print(Result result) {

        if (result.success()) {
            console.success("Compilation completed successfully.");
        } else {
            console.error("Compilation failed.");
        }

        if (!result.diagnostics().isEmpty()) {

            console.print("");

            for (String diagnostic : result.diagnostics()) {
                console.print(diagnostic);
            }
        }
    }
}
