package io.devinebyte.runtime.module;

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
}
