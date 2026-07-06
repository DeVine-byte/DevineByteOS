package org.devinebyte.compiler.parser.parser;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public class ParserException extends RuntimeException {

    public ParserException(String message) {
        super(message);
    }

}
