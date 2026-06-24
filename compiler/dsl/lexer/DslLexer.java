package compiler.dsl.lexer;

import compiler.dsl.tokens.*;

import java.util.ArrayList;
import java.util.List;

public class DslLexer {

    private final String input;
    private int pos = 0;

    public DslLexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {

        List<Token> tokens = new ArrayList<>();

        while (!isEOF()) {

            char current = peek();

            if (Character.isWhitespace(current)) {
                advance();
                continue;
            }

            if (Character.isLetter(current)) {
                tokens.add(readKeywordOrIdentifier());
                continue;
            }

            if (current == '"') {
                tokens.add(readString());
                continue;
            }

            switch (current) {
                case '{':
                    tokens.add(new Token(TokenType.LBRACE, "{"));
                    advance();
                    break;
                case '}':
                    tokens.add(new Token(TokenType.RBRACE, "}"));
                    advance();
                    break;
                case ':':
                    tokens.add(new Token(TokenType.COLON, ":"));
                    advance();
                    break;
                case '-':
                    if (peekNext() == '>') {
                        tokens.add(new Token(TokenType.ARROW, "->"));
                        advance();
                        advance();
                    }
                    break;
                default:
                    advance();
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token readKeywordOrIdentifier() {
        StringBuilder sb = new StringBuilder();

        while (!isEOF() && Character.isLetterOrDigit(peek())) {
            sb.append(advance());
        }

        String value = sb.toString();

        return switch (value) {
            case "organization" -> new Token(TokenType.ORGANIZATION, value);
            case "tenant" -> new Token(TokenType.TENANT, value);
            case "entity" -> new Token(TokenType.ENTITY, value);
            case "workflow" -> new Token(TokenType.WORKFLOW, value);
            case "state" -> new Token(TokenType.STATE, value);
            case "transition" -> new Token(TokenType.TRANSITION, value);
            case "rule" -> new Token(TokenType.RULE, value);
            case "event" -> new Token(TokenType.EVENT, value);
            case "policy" -> new Token(TokenType.POLICY, value);
            case "kpi" -> new Token(TokenType.KPI, value);
            default -> new Token(TokenType.IDENTIFIER, value);
        };
    }

    private Token readString() {
        advance(); // skip "
        StringBuilder sb = new StringBuilder();

        while (!isEOF() && peek() != '"') {
            sb.append(advance());
        }

        advance(); // closing "

        return new Token(TokenType.STRING, sb.toString());
    }

    private char peek() {
        return input.charAt(pos);
    }

    private char peekNext() {
        return input.charAt(pos + 1);
    }

    private char advance() {
        return input.charAt(pos++);
    }

    private boolean isEOF() {
        return pos >= input.length();
    }
              }
