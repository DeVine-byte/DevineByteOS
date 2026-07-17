package org.devinebyte.compiler.parser.parser;

import org.devinebyte.compiler.parser.ast.EntityNode;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.ast.PropertyNode;
import org.devinebyte.compiler.parser.ast.WorkflowNode;
import org.devinebyte.compiler.parser.lexer.DefaultLexer;
import org.devinebyte.compiler.parser.lexer.Lexer;
import org.devinebyte.compiler.parser.lexer.Token;
import org.devinebyte.compiler.parser.lexer.TokenType;

import java.util.List;

/**
 * Default implementation of the DevineByte parser.
 *
 * Converts a stream of tokens into an Abstract Syntax Tree (AST).
 */
public final class DefaultParser implements Parser {

    private final Lexer lexer = new DefaultLexer();

    private List<Token> tokens;
    private int position;

    @Override
    public ProgramNode parse(String source) {

        this.tokens = lexer.tokenize(source);
        this.position = 0;

        ProgramNode program = new ProgramNode();

        while (!isAtEnd()) {

            Token token = peek();

            switch (token.type()) {

                case ENTITY ->
                        program.getDeclarations().add(parseEntity());

                case WORKFLOW ->
                        program.getDeclarations().add(parseWorkflow());

                case EOF ->
                        advance();

                default ->
                        throw error("Unexpected token: " + token.type());
            }
        }

        return program;
    }

    private EntityNode parseEntity() {

        consume(TokenType.ENTITY, "Expected 'entity'.");

        EntityNode entity = new EntityNode();

        entity.setName(
                consume(TokenType.IDENTIFIER,
                        "Expected entity name.")
                        .value()
        );

        consume(TokenType.LEFT_BRACE,
                "Expected '{'.");

        while (!check(TokenType.RIGHT_BRACE)) {

            entity.getProperties().add(parseProperty());
        }

        consume(TokenType.RIGHT_BRACE,
                "Expected '}'.");

        return entity;
    }

    private PropertyNode parseProperty() {

        consume(TokenType.PROPERTY,
                "Expected 'property'.");

        PropertyNode property = new PropertyNode();

        property.setName(
                consume(TokenType.IDENTIFIER,
                        "Expected property name.")
                        .value()
        );

        consume(TokenType.COLON,
                "Expected ':'.");

        property.setType(
                consume(TokenType.IDENTIFIER,
                        "Expected property type.")
                        .value()
        );

        if (check(TokenType.SEMICOLON)) {
            advance();
        }

        return property;
    }

    private WorkflowNode parseWorkflow() {

        consume(TokenType.WORKFLOW,
                "Expected 'workflow'.");

        WorkflowNode workflow = new WorkflowNode();

        workflow.setName(
                consume(TokenType.IDENTIFIER,
                        "Expected workflow name.")
                        .value()
        );

        consume(TokenType.LEFT_BRACE,
                "Expected '{'.");

        while (!check(TokenType.RIGHT_BRACE)) {
            advance();
        }

        consume(TokenType.RIGHT_BRACE,
                "Expected '}'.");

        return workflow;
    }

    private Token consume(TokenType expected, String message) {

        if (!check(expected)) {
            throw error(message);
        }

        return advance();
    }

    private boolean check(TokenType type) {

        if (isAtEnd()) {
            return false;
        }

        return peek().type() == type;
    }

    private Token advance() {

        if (!isAtEnd()) {
            position++;
        }

        return previous();
    }

    private boolean isAtEnd() {

        return peek().type() == TokenType.EOF;
    }

    private Token peek() {

        return tokens.get(position);
    }

    private Token previous() {

        return tokens.get(position - 1);
    }

    private ParserException error(String message) {

        Token token = peek();

        return new ParserException(
                message +
                " Line " +
                token.line() +
                ", Column " +
                token.column()
        );
    }
}
