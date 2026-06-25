package org.devinebyte.compiler.parser.lexer;

public record Token(

        TokenType type,

        String value,

        int line,

        int column

) {}
