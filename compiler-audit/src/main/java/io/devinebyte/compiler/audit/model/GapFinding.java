package io.devinebyte.compiler.audit.model;

public record GapFinding(
    String id,
    String description,
    String severity,
    String businessUnitId
) {}
