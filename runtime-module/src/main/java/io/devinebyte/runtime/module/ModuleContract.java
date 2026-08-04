package io.devinebyte.runtime.module;

import java.util.List;

/**
 * What a module exposes to the runtime. From module_graph.json
 */
public record ModuleContract(
    List<String> exposesEvents,
    List<String> subscribesToEvents
) {
    public static ModuleContract empty() {
        return new ModuleContract(List.of(), List.of());
    }
}
