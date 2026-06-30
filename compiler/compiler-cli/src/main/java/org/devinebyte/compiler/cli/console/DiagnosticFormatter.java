package org.devinebyte.compiler.cli.console;

import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.Severity; 

public class DiagnosticFormatter {

    public String format(Diagnostic diagnostic) {

        return String.format(
                "%s: %s (%s:%d:%d)",
                diagnostic.severity(),
                diagnostic.message(),
                diagnostic.location().file(),
                diagnostic.location().line(),
                diagnostic.location().column()
        );
    }

    public void print(Console console, Diagnostic diagnostic) {

        String text = format(diagnostic);

        if (diagnostic.severity() == Severity.ERROR) {
            console.error(text);
        } else {
            console.warning(text);
        }
    }
}
