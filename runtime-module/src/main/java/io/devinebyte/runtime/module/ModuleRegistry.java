package io.devinebyte.runtime.module;

import io.devinebyte.runtime.config.ModuleGraph.ModuleDefinition; // FIX: import from config
import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ModuleRegistry {
    private final Map<String, Map<String, ModuleDefinition>> tenantModules = new ConcurrentHashMap<>();

    @Inject
    public ModuleRegistry() {}

    public void register(TenantContext tenant, Map<String, ModuleDefinition> modules) {
        tenantModules.put(tenant.tenantId(), modules);
    }

    public boolean isEnabled(TenantContext tenant, String moduleId) {
        Map<String, ModuleDefinition> modules = tenantModules.get(tenant.tenantId());
        if (modules == null) return false;
        ModuleDefinition def = modules.get(moduleId);
        return def != null && def.enabled();
    }

    public ModuleDefinition get(TenantContext tenant, String moduleId) {
        Map<String, ModuleDefinition> modules = tenantModules.get(tenant.tenantId());
        return modules == null ? null : modules.get(moduleId);
    }
}
