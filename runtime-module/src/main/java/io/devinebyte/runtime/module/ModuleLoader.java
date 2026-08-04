package io.devinebyte.runtime.module;

import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.*;

@Singleton
public class ModuleLoader {
    private final DependencyResolver resolver;

    @Inject
    public ModuleLoader(DiagnosticCollector diagnostics) {
        this.resolver = new DependencyResolver(diagnostics);
    }

    public record LoadResult(List<ModuleDefinition> loadOrder, Set<String> enabledModules) {}

    public LoadResult load(TenantContext tenant, ModuleGraph graph, DiagnosticCollector diagnostics) {
        Map<String, ModuleDefinition> all = graph.modules();
        Set<String> requested = tenant.enabledModules();
        String tenantId = tenant.tenantId();

        if (requested.isEmpty()) {
            // no warn, so just continue with empty
            return new LoadResult(List.of(), Set.of());
        }

        Map<String, ModuleDefinition> enabledMap = new LinkedHashMap<>();
        Set<String> enabledIds = new LinkedHashSet<>();

        for (String moduleId : requested) {
            ModuleDefinition def = all.get(moduleId);
            if (def == null) {
                diagnostics.error("DBRT006", "Module '" + moduleId + "' not found in module_graph.json", tenantId);
                continue;
            }
            if (!def.enabled()) {
                diagnostics.error("DBRT009", "Module '" + moduleId + "' not enabled for tenant", tenantId);
                continue;
            }
            enabledMap.put(moduleId, def);
            enabledIds.add(moduleId);
        }

        if (diagnostics.hasFatal()) {
            return new LoadResult(List.of(), Set.of());
        }

        List<ModuleDefinition> loadOrder = resolver.topoSort(enabledMap, tenantId);
        if (diagnostics.hasFatal()) {
            return new LoadResult(List.of(), Set.of());
        }

        return new LoadResult(loadOrder, enabledIds);
    }
}
