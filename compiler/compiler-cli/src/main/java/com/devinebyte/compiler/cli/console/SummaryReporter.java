package org.devinebyte.compiler.cli.console;

public class SummaryReporter {

    public void print(
            Console console,
            int files,
            int warnings,
            int errors,
            long durationMillis) {

        console.print("");

        console.print("Compilation Summary");

        console.print("-------------------");

        console.print("Files      : " + files);

        console.print("Warnings   : " + warnings);

        console.print("Errors     : " + errors);

        console.print("Duration   : " + durationMillis + " ms");

        if (errors == 0) {
            console.success("BUILD SUCCESSFUL");
        } else {
            console.error("BUILD FAILED");
        }
    }
}
