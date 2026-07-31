package io.devinebyte.compiler.core.model;

import java.util.List;
import java.util.Set;

public record ModuleDefinition(
    String id,
    String name,
    boolean enabled,
    Set<String> dependencies,
    List<String> exposedEvents,
    List<String> consumedEvents
) {}
