package io.devinebyte.runtime.tenant.registry;

import io.devinebyte.runtime.tenant.TenantRuntime;

public record TenantRuntimeHandle(String tenantId, TenantRuntime runtime) implements AutoCloseable {
    @Override
    public void close() { runtime.close(); }
}
