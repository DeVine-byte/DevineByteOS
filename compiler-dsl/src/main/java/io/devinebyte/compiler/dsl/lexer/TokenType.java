package io.devinebyte.compiler.dsl.lexer;

public enum TokenType {
    // Keywords
    MODULE, ENTITY, EVENT, WORKFLOW, KPI, ENABLE, DISABLE, EXPOSE, API,
    // HTTP Verbs
    GET, POST, PUT, DELETE, PATCH,
    // Literals
    IDENTIFIER, STRING, NUMBER,
    //token
    DEPENDS, ON,
    // Symbols
    LBRACE, RBRACE, LPAREN, RPAREN, COLON, SEMICOLON, COMMA, ARROW, LBRACK, RBRACK,
    EOF, ILLEGAL
}
