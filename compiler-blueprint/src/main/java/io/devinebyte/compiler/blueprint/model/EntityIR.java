package io.devinebyte.compiler.blueprint.model;

import java.util.Map;

public record EntityIR(
    String name,
    String moduleId,
    Map<String, String> fields,
    String aggregateRoot
) {}
