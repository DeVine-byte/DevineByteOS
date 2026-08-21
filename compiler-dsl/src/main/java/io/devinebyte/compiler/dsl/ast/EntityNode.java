package io.devinebyte.compiler.dsl.ast;

import java.util.List;
import java.util.Map;

public record EntityNode(
    String name,
    Map<String, String> fields,
    List<String> exposedMethods // NEW
) implements AstNode {}
