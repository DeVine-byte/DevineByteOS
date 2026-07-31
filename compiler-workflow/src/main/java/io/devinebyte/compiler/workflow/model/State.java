package io.devinebyte.compiler.workflow.model;

import java.util.List;

public record State(
    String name,
    boolean isInitial,
    boolean isFinal,
    List<Transition> transitions
) {}
