package org.devinebyte.compiler.parser.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the DevineByte lexer.
 *
 * Converts source text into a stream of tokens.
 *
 * Current implementation supports:
 * - keywords
 * - identifiers
 * - numbers
 * - strings
 * - braces
 * - parentheses
 * - punctuation
 *
 * Future versions will support:
 * - comments
 * - annotations
 * - imports
 * - modules
 * - generics
 */
public final class DefaultLexer implements Lexer {

    private String source;
    private int index;
    private int line;
    private int column;

    @Override
    public List<Token> tokenize(String source) {

        this.source = source == null ? "" : source;

        index = 0;
        line = 1;
        column = 1;

        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {

            char c = peek();

            if (Character.isWhitespace(c)) {
                consumeWhitespace();
                continue;
            }

            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifier());
                continue;
            }

            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            switch (c) {

                case '{':
                    tokens.add(token(TokenType.LEFT_BRACE, "{"));
                    advance();
                    break;

                case '}':
                    tokens.add(token(TokenType.RIGHT_BRACE, "}"));
                    advance();
                    break;

                case '(':
                    tokens.add(token(TokenType.LEFT_PAREN, "("));
                    advance();
                    break;

                case ')':
                    tokens.add(token(TokenType.RIGHT_PAREN, ")"));
                    advance();
                    break;

                case ':':
                    tokens.add(token(TokenType.COLON, ":"));
                    advance();
                    break;

                case ',':
                    tokens.add(token(TokenType.COMMA, ","));
                    advance();
                    break;

                case ';':
                    tokens.add(token(TokenType.SEMICOLON, ";"));
                    advance();
                    break;

                case '=':
                    tokens.add(token(TokenType.EQUALS, "="));
                    advance();
                    break;

                case '"':
                    tokens.add(readString());
                    break;

                default:
                    throw new LexerException(
                            "Unexpected character '" +
                                    c +
                                    "' at line " +
                                    line +
                                    ", column " +
                                    column
                    );
            }
        }

        tokens.add(new Token(
                TokenType.EOF,
                "",
                line,
                column
        ));

        return tokens;
    }

    private Token readIdentifier() {

        int startLine = line;
        int startColumn = column;

        StringBuilder builder = new StringBuilder();

        while (!isAtEnd()) {

            char c = peek();

            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                break;
            }

            builder.append(c);
            advance();
        }

        String value = builder.toString();

        TokenType type = switch (value) {

            case "entity" -> TokenType.ENTITY;
            case "workflow" -> TokenType.WORKFLOW;
            case "property" -> TokenType.PROPERTY;
            case "true", "false" -> TokenType.BOOLEAN;

            default -> TokenType.IDENTIFIER;
        };

        return new Token(
                type,
                value,
                startLine,
                startColumn
        );
    }

    private Token readNumber() {

        int startLine = line;
        int startColumn = column;

        StringBuilder builder = new StringBuilder();

        while (!isAtEnd() && Character.isDigit(peek())) {

            builder.append(peek());
            advance();
        }

        return new Token(
                TokenType.NUMBER,
                builder.toString(),
                startLine,
                startColumn
        );
    }

    private Token readString() {

        int startLine = line;
        int startColumn = column;

        advance();

        StringBuilder builder = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {

            builder.append(peek());
            advance();
        }

        if (isAtEnd()) {
            throw new LexerException("Unterminated string literal.");
        }

        advance();

        return new Token(
                TokenType.STRING,
                builder.toString(),
                startLine,
                startColumn
        );
    }

    private void consumeWhitespace() {

        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private Token token(TokenType type, String value) {

        return new Token(
                type,
                value,
                line,
                column
        );
    }

    private boolean isAtEnd() {
        return index >= source.length();
    }

    private char peek() {
        return source.charAt(index);
    }

    private void advance() {

        if (peek() == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        index++;
    }
}
