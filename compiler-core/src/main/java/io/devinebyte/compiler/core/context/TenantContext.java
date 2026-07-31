package io.devinebyte.compiler.core.context;

import java.util.Set;

public record TenantContext(
    String tenantId,
    TenantLifecycle state,
    Set<String> enabledModules
) {
    public boolean isModuleEnabled(String moduleId) {
        return enabledModules.contains(moduleId);
    }
}
