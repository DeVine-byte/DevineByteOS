package io.devinebyte.compiler.reporting.collector;

import io.devinebyte.compiler.blueprint.model.*;
import io.devinebyte.compiler.reporting.model.DependencyGraph;
import io.devinebyte.compiler.reporting.model.ModuleDependency;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DependencyGraphBuilder {

    public DependencyGraph build(BlueprintIR blueprint) {
        Set<ModuleDependency> edges = new HashSet<>();
        Map<String, ModuleIR> modulesById = new HashMap<>();
        Map<String, String> entityToModule = new HashMap<>();

        for (ModuleIR m : blueprint.modules()) {
            modulesById.put(m.id(), m);
            m.entities().forEach(e -> entityToModule.put(e.name(), m.id()));
        }

        // 1. Static module dependencies
        for (ModuleIR module : blueprint.modules()) {
            for (String depId : module.dependencies()) {
                ModuleIR dep = modulesById.get(depId);
                if (dep != null) {
                    edges.add(new ModuleDependency(module.name(), dep.name(), "DEPENDS_ON"));
                }
            }
        }

        // 2. Entity ownership via workflow steps - FIXED: use target module name
        for (ModuleIR module : blueprint.modules()) {
            for (WorkflowIR wf : module.workflows()) {
                for (String step : wf.steps()) {
                    entityToModule.entrySet().stream()
                        .filter(e -> step.equals(e.getKey()) || step.contains(e.getKey()))
                        .map(Map.Entry::getValue) // this is the moduleId
                        .map(modulesById::get)
                        .filter(Objects::nonNull)
                        .forEach(targetModule -> {
                            if (!targetModule.name().equals(module.name())) {
                                edges.add(new ModuleDependency(module.name(), targetModule.name(), "USES_ENTITY:" + step));
                            }
                        });
                }
            }
        }

        // 3. Event dependencies - USE requiredEvents from WorkflowIR
        for (ModuleIR consumerModule : blueprint.modules()) {
            for (WorkflowIR wf : consumerModule.workflows()) {
                for (String eventName : wf.requiredEvents()) {
                    blueprint.events().stream()
                        .filter(e -> e.name().equals(eventName))
                        .findFirst()
                        .ifPresent(event -> {
                            ModuleIR producerModule = modulesById.get(event.moduleId());
                            if (producerModule != null && !producerModule.id().equals(consumerModule.id())) {
                                edges.add(new ModuleDependency(producerModule.name(), consumerModule.name(), "EVENT:" + eventName));
                            }
                        });
                }
            }
        }

        List<String> topo = topologicalSort(modulesById.values().stream().map(ModuleIR::name).collect(Collectors.toSet()), edges);
        return new DependencyGraph(edges, topo);
    }

    private List<String> topologicalSort(Set<String> nodes, Set<ModuleDependency> edges) {
        Map<String, Integer> inDegree = new HashMap<>();
        nodes.forEach(n -> inDegree.put(n, 0));

        edges.stream().filter(e -> e.contractType().equals("DEPENDS_ON"))
             .forEach(e -> inDegree.compute(e.toModule(), (k,v) -> v == null ? 1 : v + 1));

        Queue<String> queue = new LinkedList<>();
        inDegree.entrySet().stream().filter(e -> e.getValue() == 0).map(Map.Entry::getKey).forEach(queue::add);

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String n = queue.poll();
            result.add(n);
            for (ModuleDependency e : edges) {
                if (e.fromModule().equals(n) && e.contractType().equals("DEPENDS_ON")) {
                    inDegree.compute(e.toModule(), (k,v) -> v - 1);
                    if (inDegree.get(e.toModule()) == 0) queue.add(e.toModule());
                }
            }
        }
        return result;
    }
}
