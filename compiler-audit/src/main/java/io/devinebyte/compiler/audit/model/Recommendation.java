package io.devinebyte.compiler.audit.model;

public record Recommendation(
    String id,
    String description,
    String type,
    String targetModule
) {}
