package io.devinebyte.compiler.workflow.model;

public record Transition(
    String triggerEvent,
    String targetState,
    String action // name of command/handler to execute
) {}
