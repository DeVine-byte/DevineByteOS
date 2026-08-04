package io.devinebyte.compiler.dsl;

import io.devinebyte.compiler.dsl.lexer.TokenType;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class KeywordDictionary {
    private final Map<String, TokenType> baseKeywords = new HashMap<>();
    private final Map<String, TokenType> activeKeywords = new HashMap<>();

    public KeywordDictionary() {
        baseKeywords.put("module", TokenType.MODULE);
        baseKeywords.put("entity", TokenType.ENTITY);
        baseKeywords.put("event", TokenType.EVENT);
        baseKeywords.put("workflow", TokenType.WORKFLOW);
        baseKeywords.put("kpi", TokenType.KPI);
        baseKeywords.put("enable", TokenType.ENABLE);
        baseKeywords.put("enabled", TokenType.ENABLE); 
        baseKeywords.put("disable", TokenType.DISABLE);
        baseKeywords.put("disabled", TokenType.DISABLE);
        baseKeywords.put("depends", TokenType.DEPENDS);
        baseKeywords.put("on", TokenType.ON);
        reset();
    }

    public void reset() {
        activeKeywords.clear();
        activeKeywords.putAll(baseKeywords);
    }

    public void loadAliases(Map<String, String> aliases) {
        reset();
        aliases.forEach((alias, canonical) -> {
            TokenType type = baseKeywords.get(canonical.toLowerCase());
            if (type != null) {
                activeKeywords.put(alias.toLowerCase(), type);
            }
        });
    }

    public TokenType lookup(String lexeme) {
        return activeKeywords.getOrDefault(lexeme.toLowerCase(), TokenType.IDENTIFIER);
    }
}
