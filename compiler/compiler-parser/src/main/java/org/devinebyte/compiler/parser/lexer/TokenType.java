package org.devinebyte.compiler.parser.lexer;

public enum TokenType {

    ENTITY,
    WORKFLOW,
    PROPERTY,
    IDENTIFIER,
    STRING,
    NUMBER,
    BOOLEAN,

    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_PAREN,
    RIGHT_PAREN,

    COLON,
    COMMA,
    SEMICOLON,

    EQUALS,

    EOF
}
