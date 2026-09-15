package io.devinebyte.compiler.dsl.lexer;

public enum TokenType {
    // Legacy Keywords
    MODULE, ENTITY, EVENT, WORKFLOW, KPI, ENABLE, DISABLE, EXPOSE, API,
    
    // ITEM 16 FIXED: Enhanced Production v1.0.0 Core Keywords
    SERVICE, POLICY, PERMISSION, RELATION,
    
    // HTTP Verbs
    GET, POST, PUT, DELETE, PATCH,
    // Literals
    IDENTIFIER, STRING, NUMBER,
    // Dependency links
    DEPENDS, ON,
    // Symbols & Validation Annotations
    LBRACE, RBRACE, LPAREN, RPAREN, COLON, SEMICOLON, COMMA, ARROW, LBRACK, RBRACK,
    AT_NOTNULL,
    EOF, ILLEGAL
}

