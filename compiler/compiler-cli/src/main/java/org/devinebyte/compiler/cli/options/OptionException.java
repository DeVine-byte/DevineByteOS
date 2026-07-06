package org.devinebyte.compiler.cli.options;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public class OptionException extends RuntimeException {

    public OptionException(String message) {
        super(message);
    }
}
