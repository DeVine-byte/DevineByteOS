package io.devinebyte.compiler.dsl.ast;

import java.util.List;
import java.util.Set;

public record ModuleNode(
    String name,
    boolean enabled,
    Set<String> dependencies, // ADD THIS LINE
    List<AstNode> children
) implements AstNode {}
