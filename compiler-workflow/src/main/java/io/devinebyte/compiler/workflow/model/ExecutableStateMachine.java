package io.devinebyte.compiler.workflow.model;

import java.util.List;

public record ExecutableStateMachine(
    String workflowName,
    String moduleId, 
    String version,
    List<State> states
) {}
