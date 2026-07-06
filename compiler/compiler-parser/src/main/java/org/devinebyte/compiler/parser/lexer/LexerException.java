package org.devinebyte.compiler.parser.lexer;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public class LexerException extends RuntimeException {

    public LexerException(String message) {
        super(message);
    }

}
