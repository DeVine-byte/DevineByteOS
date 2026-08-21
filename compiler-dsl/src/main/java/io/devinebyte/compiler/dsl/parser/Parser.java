package io.devinebyte.compiler.dsl.parser;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.ast.*;
import io.devinebyte.compiler.dsl.lexer.Token;
import io.devinebyte.compiler.dsl.lexer.TokenType;
import jakarta.inject.Singleton;
import java.util.*;

@Singleton
public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    public Parser(List<Token> tokens) { this.tokens = tokens; }

    public List<AstNode> parse(CompilationContext context) {
        List<AstNode> nodes = new ArrayList<>();
        while (!isAtEnd()) { nodes.add(declaration(context)); }
        return nodes;
    }

    private AstNode declaration(CompilationContext context) {
        if (match(TokenType.MODULE)) return moduleDeclaration(context);
        if (match(TokenType.ENTITY)) return entityDeclaration(context);
        if (match(TokenType.EVENT)) return eventDeclaration(context);
        if (match(TokenType.WORKFLOW)) return workflowDeclaration(context);
        if (match(TokenType.KPI)) return kpiDeclaration(context);
        context.diagnostics().addError("PARSER_001", "Expected declaration");
        throw new ParseException("Parse error at line " + peek().line());
    }

    private ModuleNode moduleDeclaration(CompilationContext context) {
        String name = consume(TokenType.IDENTIFIER, "Expected module name").lexeme();
        Set<String> dependencies = new HashSet<>();
        if (match(TokenType.DEPENDS)) {
            consume(TokenType.ON, "Expected 'on' after 'depends'");
            do { dependencies.add(consume(TokenType.IDENTIFIER, "Expected module name").lexeme().toLowerCase()); }
            while (match(TokenType.COMMA));
        }
        boolean enabled = true;
        if (match(TokenType.ENABLE)) enabled = true;
        else if (match(TokenType.DISABLE)) enabled = false;
        consume(TokenType.LBRACE, "Expected '{'");
        List<AstNode> children = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) children.add(declaration(context));
        consume(TokenType.RBRACE, "Expected '}'");
        return new ModuleNode(name, enabled, dependencies, children);
    }

    private EntityNode entityDeclaration(CompilationContext context) {
        String name = consume(TokenType.IDENTIFIER, "Expected entity name").lexeme();
        consume(TokenType.LBRACE, "Expected '{'");
        Map<String, String> fields = new HashMap<>();
        List<String> exposed = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (match(TokenType.EXPOSE)) {
                consume(TokenType.API, "Expected 'api'");
                consume(TokenType.LBRACK, "Expected '['"); // FIXED: LBRACK
                do { 
                    if (!match(TokenType.GET, TokenType.POST, TokenType.PUT, TokenType.DELETE, TokenType.PATCH)) {
                        throw new ParseException("Expected HTTP method at line " + peek().line());
                    }
                    exposed.add(previous().lexeme().toUpperCase()); 
                }
                while (match(TokenType.COMMA));
                consume(TokenType.RBRACK, "Expected ']'"); // FIXED: RBRACK
                match(TokenType.SEMICOLON);
            } else {
                String fieldName = consume(TokenType.IDENTIFIER, "Expected field name").lexeme();
                consume(TokenType.COLON, "Expected ':'");
                String fieldType = consume(TokenType.IDENTIFIER, "Expected field type").lexeme();
                fields.put(fieldName, fieldType);
                match(TokenType.SEMICOLON);
            }
        }
        consume(TokenType.RBRACE, "Expected '}'");
        return new EntityNode(name, fields, exposed);
    }

    private EventNode eventDeclaration(CompilationContext context) {
        String name = consume(TokenType.IDENTIFIER, "Expected event name").lexeme();
        Map<String, String> payload = new HashMap<>();
        if (match(TokenType.LPAREN)) { consume(TokenType.RPAREN, "Expected ')'"); }
        else {
            consume(TokenType.LBRACE, "Expected '{'");
            if (match(TokenType.IDENTIFIER) && previous().lexeme().equals("payload")) {
                consume(TokenType.COLON, "Expected ':'"); consume(TokenType.LBRACE, "Expected '{'");
                while (!check(TokenType.RBRACE) && !isAtEnd()) {
                    String fieldName = consume(TokenType.IDENTIFIER, "Expected field name").lexeme();
                    consume(TokenType.COLON, "Expected ':'");
                    String fieldType = consume(TokenType.IDENTIFIER, "Expected field type").lexeme();
                    payload.put(fieldName, fieldType); match(TokenType.COMMA);
                }
                consume(TokenType.RBRACE, "Expected '}'");
            }
            consume(TokenType.RBRACE, "Expected '}'");
        }
        match(TokenType.SEMICOLON);
        return new EventNode(name, payload);
    }

    private WorkflowNode workflowDeclaration(CompilationContext context) {
        String name = consume(TokenType.IDENTIFIER, "Expected workflow name").lexeme();
        consume(TokenType.LBRACE, "Expected '{'");
        List<String> steps = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (check(TokenType.IDENTIFIER) && peek().lexeme().equals("step")) advance();
            String stepName = consume(TokenType.IDENTIFIER, "Expected step name").lexeme();
            steps.add(stepName); match(TokenType.ARROW); match(TokenType.SEMICOLON);
        }
        consume(TokenType.RBRACE, "Expected '}'");
        return new WorkflowNode(name, steps);
    }

    private KpiNode kpiDeclaration(CompilationContext context) {
        String name = consume(TokenType.IDENTIFIER, "Expected KPI name").lexeme();
        consume(TokenType.COLON, "Expected ':'");
        StringBuilder formula = new StringBuilder();
        while (!check(TokenType.RBRACE) && !check(TokenType.SEMICOLON) && !isAtEnd()) formula.append(advance().lexeme()).append(" ");
        match(TokenType.SEMICOLON);
        return new KpiNode(name, formula.toString().trim());
    }

    private Token consume(TokenType type, String message) { if (check(type)) return advance(); throw new ParseException(message + " at line " + peek().line()); }
    private boolean match(TokenType... types) { for (TokenType type : types) { if (check(type)) { advance(); return true; } } return false; }
    private boolean check(TokenType type) { return !isAtEnd() && peek().type() == type; }
    private Token advance() { if (!isAtEnd()) current++; return tokens.get(current - 1); }
    private Token previous() { return tokens.get(current - 1); }
    private boolean isAtEnd() { return peek().type() == TokenType.EOF; }
    private Token peek() { return tokens.get(current); }
}
