package io.devinebyte.compiler.dsl.ast;

public record KpiNode(
    String name,
    String formula
) implements AstNode {}
