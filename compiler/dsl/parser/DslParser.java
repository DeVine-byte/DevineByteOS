package compiler.dsl.parser;

import compiler.dsl.tokens.*;
import compiler.dsl.ast.*;

import java.util.*;

public class DslParser {

    private final List<Token> tokens;
    private int index = 0;

    public DslParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public OrganizationNode parse() {

        consume(TokenType.ORGANIZATION);

        String orgName = consume(TokenType.STRING).getValue();

        consume(TokenType.LBRACE);

        List<AstNode> children = new ArrayList<>();

        while (!match(TokenType.RBRACE)) {

            Token token = peek();

            switch (token.getType()) {

                case ENTITY -> children.add(parseEntity());
                case WORKFLOW -> children.add(parseWorkflow());
                default -> advance();
            }
        }

        consume(TokenType.RBRACE);

        return new OrganizationNode(orgName, children);
    }

    private EntityNode parseEntity() {

        consume(TokenType.ENTITY);
        String name = consume(TokenType.IDENTIFIER).getValue();

        consume(TokenType.LBRACE);

        Map<String, String> fields = new HashMap<>();

        while (!match(TokenType.RBRACE)) {

            String key = consume(TokenType.IDENTIFIER).getValue();
            consume(TokenType.COLON);
            String value = consume(TokenType.IDENTIFIER).getValue();

            fields.put(key, value);
        }

        consume(TokenType.RBRACE);

        return new EntityNode(name, fields);
    }

    private WorkflowNode parseWorkflow() {

        consume(TokenType.WORKFLOW);
        String name = consume(TokenType.IDENTIFIER).getValue();

        consume(TokenType.LBRACE);

        List<String> states = new ArrayList<>();
        List<String> transitions = new ArrayList<>();

        while (!match(TokenType.RBRACE)) {

            Token t = peek();

            if (t.getType() == TokenType.STATE) {
                advance();
                states.add(consume(TokenType.IDENTIFIER).getValue());
            }

            if (t.getType() == TokenType.TRANSITION) {
                advance();
                String from = consume(TokenType.IDENTIFIER).getValue();
                consume(TokenType.ARROW);
                String to = consume(TokenType.IDENTIFIER).getValue();

                transitions.add(from + "->" + to);
            }
        }

        consume(TokenType.RBRACE);

        return new WorkflowNode(name, states, transitions);
    }

    private Token consume(TokenType type) {
        Token t = tokens.get(index++);
        return t;
    }

    private Token peek() {
        return tokens.get(index);
    }

    private boolean match(TokenType type) {
        return peek().getType() == type;
    }

    private void advance() {
        index++;
    }
}
