package compiler.dsl.tokens;

public enum TokenType {

    IDENTIFIER,
    STRING,
    NUMBER,

    ORGANIZATION,
    TENANT,
    ENTITY,
    WORKFLOW,
    STATE,
    TRANSITION,
    RULE,
    EVENT,
    POLICY,
    KPI,

    LBRACE,
    RBRACE,
    COLON,
    ARROW,

    EOF
}
