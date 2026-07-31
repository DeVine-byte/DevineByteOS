package io.devinebyte.compiler.dsl.ast;

import java.util.Map;

public record EntityNode(
    String name,
    Map<String, String> fields
) implements AstNode {}
