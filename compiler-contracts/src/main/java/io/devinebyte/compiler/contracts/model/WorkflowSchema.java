package io.devinebyte.compiler.contracts.model;

import java.util.List;

public record WorkflowSchema(
    String name,
    List<Step> steps
) {}
