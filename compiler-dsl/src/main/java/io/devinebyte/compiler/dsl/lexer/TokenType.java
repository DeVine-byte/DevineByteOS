package io.devinebyte.compiler.dsl.lexer;

public enum TokenType {
    // Keywords
    MODULE, ENTITY, EVENT, WORKFLOW, KPI, ENABLE, DISABLE,
    // Literals
    IDENTIFIER, STRING, NUMBER,
    //token
    DEPENDS, ON,
    // Symbols
    LBRACE, RBRACE, LPAREN, RPAREN, COLON, SEMICOLON, COMMA, ARROW,
    EOF, ILLEGAL
}
