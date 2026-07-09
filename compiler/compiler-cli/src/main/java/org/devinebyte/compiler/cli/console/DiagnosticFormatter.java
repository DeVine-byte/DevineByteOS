package org.devinebyte.compiler.cli.console;

import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;
import org.devinebyte.sdk.diagnostics.SourceLocation;

public final class DiagnosticFormatter {

    /**
     * Formats a diagnostic into a human-readable string.
     */
    public String format(Diagnostic diagnostic) {

        SourceLocation location = diagnostic.getLocation();

        String position = "";

        if (location != null) {
            position = String.format(
                    "%s:%d:%d",
                    location.getFile(),
                    location.getLine(),
                    location.getColumn()
            );
        }

        return String.format(
                "[%s] %s%s",
                diagnostic.getSeverity(),
                diagnostic.getMessage(),
                position.isEmpty() ? "" : " (" + position + ")"
        );
    }

    /**
     * Prints a formatted diagnostic using the appropriate console method.
     */
    public void print(Console console, Diagnostic diagnostic) {

        String text = format(diagnostic);

        switch (diagnostic.getSeverity()) {

            case ERROR:
                console.error(text);
                break;

            case WARNING:
                console.warning(text);
                break;

            case INFO:
            default:
                console.info(text);
                break;
        }
    }
}
