package io.devinebyte.compiler.reporting.model;

import java.util.List;
import java.util.Set;

public record DependencyGraph(
    Set<ModuleDependency> edges,
    List<String> topologicalOrder
) {
    public static DependencyGraph empty() {
        return new DependencyGraph(Set.of(), List.of());
    }
}
