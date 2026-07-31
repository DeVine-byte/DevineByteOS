package io.devinebyte.compiler.dsl.ast;

import java.util.Map;

public record EventNode(
    String name,
    Map<String, String> payload
) implements AstNode {}
