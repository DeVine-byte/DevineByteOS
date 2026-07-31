package io.devinebyte.runtime.tenant.registry;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.tenant.TenantRuntime;
import io.devinebyte.runtime.tenant.exception.TenantNotFoundException;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TenantRegistry {
    private record TenantRuntimeHandle(TenantRuntime runtime) {}
    private final Map<String, TenantRuntimeHandle> tenants = new ConcurrentHashMap<>();

    public void register(String tenantId, TenantRuntime runtime) {
        tenants.put(tenantId, new TenantRuntimeHandle(runtime));
    }

    public TenantRuntime get(TenantContext tenant) {
        return get(tenant.tenantId());
    }

    public TenantRuntime get(String tenantId) {
        TenantRuntimeHandle handle = tenants.get(tenantId);
        if (handle == null) throw new TenantNotFoundException("DBRT004", tenantId);
        return handle.runtime();
    }

    public void unregister(String tenantId) {
        tenants.remove(tenantId);
    }
}
