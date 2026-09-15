package io.devinebyte.runtime.module;

import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.module.exception.ModuleDisabledAccessException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ModuleIsolationGuard {
    private final ModuleRegistry registry;

    @Inject
    public ModuleIsolationGuard(ModuleRegistry registry) {
        this.registry = registry;
    }

    public void assertEnabled(TenantContext tenant, String moduleId, String operation) {
        if (!registry.isEnabled(tenant, moduleId)) {
            throw new ModuleDisabledAccessException(
                "DBRT009",
                "Module " + moduleId + " is disabled. Cannot perform: " + operation,
                tenant.tenantId()
            );
        }
    }

    /**
     * Enforces strict cross-module security and isolation barriers.
     * Prevents unauthorized modules from communicating unless explicit dependency paths are declared.
     */
    public void assertAccessPermitted(TenantContext tenant, String sourceModuleId, String targetModuleId, String operation) {
        // 1. Same-module local communication inside its own sandbox boundary is inherently safe
        if (sourceModuleId.equalsIgnoreCase(targetModuleId)) {
            return;
        }

        // 2. Ensure both participating modules are actively authorized and online for this tenant
        assertEnabled(tenant, sourceModuleId, operation);
        assertEnabled(tenant, targetModuleId, operation);

        // 3. Resolve definition metadata parameters from the active runtime topology registry
        ModuleDefinition sourceDef = registry.get(tenant, sourceModuleId);
        
        // 4. Verify an explicit architectural dependency layout exists
        boolean hasDependencyLink = sourceDef != null 
            && sourceDef.dependsOn() != null 
            && sourceDef.dependsOn().stream().anyMatch(dep -> dep.equalsIgnoreCase(targetModuleId));

        if (!hasDependencyLink) {
            throw new ModuleDisabledAccessException(
                "DBRT010",
                String.format("Cross-Module Isolation Violation: Source module '%s' is not authorized to interact with target module '%s'. Missing explicit dependency link declaration in topology graph. Action denied: %s", 
                    sourceModuleId, targetModuleId, operation),
                tenant.tenantId()
            );
        }
    }
}

