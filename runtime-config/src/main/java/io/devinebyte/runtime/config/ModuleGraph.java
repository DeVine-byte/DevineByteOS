package io.devinebyte.runtime.config;

import java.util.Map;
import java.util.Set;

public record ModuleGraph(Map<String, ModuleDefinition> modules) {
    
    public record ModuleDefinition(
        String moduleId, 
        boolean enabled, 
        Set<String> dependsOn, 
        Set<String> exposesEvents, 
        Set<String> subscribesToEvents
    ) {}

    public Set<String> enabledModules() {
        return modules.entrySet().stream()
            .filter(e -> e.getValue().enabled())
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    public boolean isEnabled(String moduleId) { 
        ModuleDefinition def = modules.get(moduleId);
        return def != null && def.enabled(); 
    }
    
    public ModuleDefinition get(String moduleId) {
        return modules.get(moduleId);
    }
}
