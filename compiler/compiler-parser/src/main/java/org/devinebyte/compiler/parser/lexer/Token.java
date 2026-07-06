package org.devinebyte.compiler.parser.lexer;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public record Token(

        TokenType type,

        String value,

        int line,

        int column

) {}
