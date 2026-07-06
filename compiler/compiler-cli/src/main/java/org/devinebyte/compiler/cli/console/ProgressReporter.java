package org.devinebyte.compiler.cli.console;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public class ProgressReporter {

    private final Console console;

    public ProgressReporter(Console console) {
        this.console = console;
    }

    public void start(String task) {
        console.info("[START] " + task);
    }

    public void step(String task) {
        console.print("  -> " + task);
    }

    public void finish(String task) {
        console.success("[DONE] " + task);
    }
}
