package org.devinebyte.compiler.parser.semantic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public class SemanticException extends RuntimeException {

    public SemanticException(String message) {
        super(message);
    }

}
