package io.devinebyte.compiler.reporting.model;

public record ModuleDependency(
    String fromModule,
    String toModule,
    String contractType // "EVENT" | "ENTITY" | "WORKFLOW" | "API"
) {}
