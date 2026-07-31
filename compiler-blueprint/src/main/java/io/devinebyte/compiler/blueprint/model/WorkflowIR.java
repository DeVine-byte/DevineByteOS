package io.devinebyte.compiler.blueprint.model;

import java.util.List;

public record WorkflowIR(
    String name,
    String moduleId,
    List<String> steps,
    List<String> requiredEvents
) {}
