package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.compiler.cli.console.Console;
import org.devinebyte.compiler.cli.console.DiagnosticFormatter;
import org.devinebyte.compiler.sdk.diagnostics.Diagnostic;
import org.devinebyte.compiler.sdk.result.CompilerResult;

public class ResultPrinter {

    private final Console console;

    private final DiagnosticFormatter formatter =
            new DiagnosticFormatter();

    public ResultPrinter(Console console) {
        this.console = console;
    }

    public void print(CompilerResult result) {

        for (Diagnostic diagnostic : result.diagnostics()) {
            formatter.print(console, diagnostic);
        }

        if (result.success()) {
            console.success("Compilation completed successfully.");
        } else {
            console.error("Compilation failed.");
        }
    }
}
