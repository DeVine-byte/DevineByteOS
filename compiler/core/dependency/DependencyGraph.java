package compiler.core.dependency;

import java.util.*;

public class DependencyGraph {

    private final Map<String, List<String>> graph = new HashMap<>();

    public void addNode(String node) {
        graph.putIfAbsent(node, new ArrayList<>());
    }

    public void addDependency(String from, String to) {
        graph.putIfAbsent(from, new ArrayList<>());
        graph.get(from).add(to);
    }

    public List<String> getDependencies(String node) {
        return graph.getOrDefault(node, List.of());
    }

    public boolean hasCircularDependency() {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();

        for (String node : graph.keySet()) {
            if (dfs(node, visited, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfs(String node, Set<String> visited, Set<String> stack) {
        if (stack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        stack.add(node);

        for (String dep : getDependencies(node)) {
            if (dfs(dep, visited, stack)) return true;
        }

        stack.remove(node);
        return false;
    }
}
