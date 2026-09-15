package io.devinebyte.compiler.dsl;

import io.devinebyte.compiler.dsl.lexer.TokenType;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class KeywordDictionary {
    private final Map<String, TokenType> keywords = new HashMap<>();
    
    public KeywordDictionary() { reset(); }
    
    public void reset() {
        keywords.clear();
        keywords.put("MODULE", TokenType.MODULE);
        keywords.put("ENTITY", TokenType.ENTITY);
        keywords.put("EVENT", TokenType.EVENT);
        keywords.put("WORKFLOW", TokenType.WORKFLOW);
        keywords.put("KPI", TokenType.KPI);
        keywords.put("ENABLE", TokenType.ENABLE);
        keywords.put("DISABLE", TokenType.DISABLE);
        keywords.put("DEPENDS", TokenType.DEPENDS);
        keywords.put("ON", TokenType.ON);
        keywords.put("EXPOSE", TokenType.EXPOSE);
        keywords.put("API", TokenType.API);
        keywords.put("GET", TokenType.GET);
        keywords.put("POST", TokenType.POST);
        keywords.put("PUT", TokenType.PUT);
        keywords.put("DELETE", TokenType.DELETE);
        keywords.put("PATCH", TokenType.PATCH);
        
        // ITEM 16 FIXED: Standardized Governance & Security Key Bindings
        keywords.put("SERVICE", TokenType.SERVICE);
        keywords.put("POLICY", TokenType.POLICY);
        keywords.put("PERMISSION", TokenType.PERMISSION);
        keywords.put("RELATION", TokenType.RELATION);
    }
    
    public TokenType lookup(String text) { 
        return keywords.getOrDefault(text, TokenType.IDENTIFIER); 
    }
    
    public void loadAliases(Map<String, String> aliases) {
       if (aliases == null || aliases.isEmpty()) return;
       reset();
       aliases.forEach((k,v) -> keywords.put(k.toUpperCase(), TokenType.valueOf(v.toUpperCase())));
    }
}

