package io.devinebyte.compiler.audit.model;

public record RiskFinding(
    String id,
    String description,
    String category,
    int probability
) {}
