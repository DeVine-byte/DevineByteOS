package io.devinebyte.compiler.dsl.lexer;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.KeywordDictionary;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class Lexer {
    private final KeywordDictionary dict;
    private String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0, current = 0, line = 1, column = 1;

    @Inject
    public Lexer(KeywordDictionary dict) {
        this.dict = dict;
        this.source = "";
    }

    public List<Token> scanTokens(CompilationContext context) {
        // Load tenant keyword aliases from attributes before scanning
        @SuppressWarnings("unchecked")
        Map<String, String> aliases = context.get("keywordAliases");
        if (aliases != null) {
            dict.loadAliases(aliases);
        } else {
            dict.reset(); // use base keywords only
        }

        // Reset state for new scan
        this.source = context.get("sourceCode");
        if (this.source == null) this.source = "";
        this.source = this.source.replace("\uFEFF", "");
        this.tokens.clear();
        this.start = 0;
        this.current = 0;
        this.line = 1;
        this.column = 1;

        while (!isAtEnd()) {
            start = current;
            scanToken(context);
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private void scanToken(CompilationContext context) {
        char c = advance();
        switch (c) {
            case '{' -> addToken(TokenType.LBRACE);
            case '}' -> addToken(TokenType.RBRACE);
            case '(' -> addToken(TokenType.LPAREN);
            case ')' -> addToken(TokenType.RPAREN);
            case ':' -> addToken(TokenType.COLON);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ',' -> addToken(TokenType.COMMA);
            case '-' -> { if (match('>')) addToken(TokenType.ARROW); }
            case ' ', '\r', '\t' -> {}
            case '\n' -> { line++; column = 1; }
            case '"' -> string(context);
            default -> {
                if (isAlpha(c)) identifier();
                else if (isDigit(c)) number();
                else context.diagnostics().addError("LEXER_001", "Unexpected character: " + c);
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);

        // CHANGED: use dictionary instead of hardcoded switch
        TokenType type = dict.lookup(text);
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        addToken(TokenType.NUMBER);
    }

    private void string(CompilationContext context) {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            advance();
        }
        if (isAtEnd()) {
            context.diagnostics().addError("LEXER_002", "Unterminated string");
            return;
        }
        advance();
        addToken(TokenType.STRING, source.substring(start + 1, current - 1));
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        column++;
        return true;
    }

    private char peek() {
        return isAtEnd()? '\0' : source.charAt(current);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        current++;
        column++;
        return source.charAt(current - 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, String literal) {
        tokens.add(new Token(type, literal != null ? literal : source.substring(start, current), line, column));
    }
}
