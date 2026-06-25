package com.devinebyte.compiler.parser.semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Object> symbols = new HashMap<>();

    public void define(String name, Object symbol) {
        symbols.put(name, symbol);
    }

    public Object resolve(String name) {
        return symbols.get(name);
    }
}
