package io.devinebyte.runtime.core.context;

import java.util.Set;

/**
 * The identity of the tenant for every operation in the runtime.
 * Must be the first parameter of every public method that touches data.
 */
public record TenantContext(
    String tenantId,
    TenantLifecycle state,
    Set<String> enabledModules
) {
    public TenantContext {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be blank");
        }
        if (enabledModules == null) {
            enabledModules = Set.of();
        }
    }

    // Factory for replay where we only know the tenantId
    public static TenantContext of(String tenantId) {
        return new TenantContext(tenantId, TenantLifecycle.ACTIVE, Set.of());
    }

    public boolean isModuleEnabled(String moduleId) {
        return enabledModules.contains(moduleId);
    }
}
