package io.devinebyte.compiler.audit.model;

public record KpiDefinition(
    String id,
    String name,
    String formula,
    String target
) {}
