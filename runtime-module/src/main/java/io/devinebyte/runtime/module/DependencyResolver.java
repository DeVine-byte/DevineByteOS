package io.devinebyte.runtime.module;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import java.util.*;

public class DependencyResolver {
    private final DiagnosticCollector diagnostics;

    public DependencyResolver(DiagnosticCollector diagnostics) {
        this.diagnostics = diagnostics;
    }

    public List<ModuleDefinition> topoSort(Map<String, ModuleDefinition> modules, String tenantId) {
        List<ModuleDefinition> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (ModuleDefinition module : modules.values()) {
            if (module.enabled()) {
                if (!dfs(module.moduleId(), modules, visited, visiting, result, tenantId)) {
                    return List.of(); // cycle detected, diagnostics already added
                }
            }
        }

        System.out.println("[DBRT] Topological Load Order: " + result.stream().map(ModuleDefinition::moduleId).toList());
        return result;
    }

    private boolean dfs(String id, Map<String, ModuleDefinition> modules, Set<String> visited,
                        Set<String> visiting, List<ModuleDefinition> result, String tenantId) {
        if (visited.contains(id)) return true;
        if (visiting.contains(id)) {
            diagnostics.error("PKG003", "Dependency cycle detected at module: " + id, tenantId);
            return false;
        }
        visiting.add(id);
        ModuleDefinition mod = modules.get(id);
        if (mod!= null) {
            for (String dep : mod.dependsOn()) {
                if (!modules.containsKey(dep)) {
                    diagnostics.error("DBRT005", "Unresolved dependency '" + dep + "' for module '" + id + "'", tenantId);
                    return false;
                }
                if (!dfs(dep, modules, visited, visiting, result, tenantId)) return false;
            }
        }
        visiting.remove(id);
        visited.add(id);
        result.add(mod);
        return true;
    }
}
