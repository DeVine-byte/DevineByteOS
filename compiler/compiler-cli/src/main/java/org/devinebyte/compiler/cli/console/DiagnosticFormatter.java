package org.devinebyte.compiler.cli.console;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity; 

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

        if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
            console.error(text);
        } else {
            console.warning(text);
        }
    }
}
