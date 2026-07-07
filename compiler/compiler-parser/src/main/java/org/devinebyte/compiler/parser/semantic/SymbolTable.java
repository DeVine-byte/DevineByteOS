package org.devinebyte.compiler.parser.semantic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SymbolTable {
    private final Map<String, Object> symbols = new HashMap<>();

    public boolean isDeclared(String name) {
        return symbols.containsKey(name);
    }

    public void define(String name, Object symbol) {
        symbols.put(name, symbol);
    }

    public Optional<Object> resolve(String name) {
        return Optional.ofNullable(symbols.get(name));
    }
}
