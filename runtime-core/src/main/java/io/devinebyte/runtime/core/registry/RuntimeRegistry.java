package io.devinebyte.runtime.core.registry;

import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry. Everything loaded from .dbpkg is registered here.
 */
@Singleton
public class RuntimeRegistry {
    private final Map<String, Object> entities = new ConcurrentHashMap<>();
    private final Map<String, Object> workflows = new ConcurrentHashMap<>();
    private final Map<String, Object> projections = new ConcurrentHashMap<>();

    public void registerEntity(TenantContext tenant, String name, Object entity) {
        entities.put(tenant.tenantId() + ":" + name, entity);
    }

    public void registerWorkflow(TenantContext tenant, String name, Object workflow) {
        workflows.put(tenant.tenantId() + ":" + name, workflow);
    }

    public void registerProjection(TenantContext tenant, String name, Object projection) {
        projections.put(tenant.tenantId() + ":" + name, projection);
    }
}
