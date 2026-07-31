package io.devinebyte.compiler.audit.model;

import java.util.List;

public record ProcessDefinition(
    String id,
    String name,
    String businessUnitId,
    List<String> steps,
    List<String> involvedEntities
) {}
