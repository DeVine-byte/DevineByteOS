package io.devinebyte.compiler.blueprint.model;

import java.util.Map;

public record EventIR(
    String name,
    String version,
    String moduleId,
    Map<String, String> payload,
    boolean isDomainEvent
) {}
