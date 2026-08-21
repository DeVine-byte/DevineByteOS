package io.devinebyte.runtime.tenant.registry;

import io.devinebyte.runtime.tenant.TenantRuntime;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TenantRegistry {
    private final Map<String, TenantRuntimeHandle> tenants = new ConcurrentHashMap<>();

    @Inject
    public TenantRegistry() {}

    public TenantRuntimeHandle register(String tenantId, TenantRuntime runtime) {
        TenantRuntimeHandle handle = new TenantRuntimeHandle(tenantId, runtime);
        tenants.put(tenantId, handle);
        return handle;
    }

    public void unregister(String tenantId) {
        TenantRuntimeHandle handle = tenants.remove(tenantId);
        if (handle != null) handle.close();
    }

    public TenantRuntime get(String tenantId) {
        TenantRuntimeHandle handle = tenants.get(tenantId);
        return handle == null ? null : handle.runtime();
    }
}
