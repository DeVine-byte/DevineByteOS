package io.devinebyte.compiler.dsl.ast;

import java.util.List;

public record WorkflowNode(
    String name,
    List<String> steps
) implements AstNode {}
