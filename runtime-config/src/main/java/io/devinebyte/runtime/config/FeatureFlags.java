package io.devinebyte.runtime.config;
import java.util.Map;
import java.util.Set;
public record FeatureFlags(Map<String, Boolean> modules) {
    public boolean isEnabled(String module) { return modules.getOrDefault(module, false); }
    public Set<String> enabledModules() {
        return modules.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(java.util.stream.Collectors.toSet());
    }
}
