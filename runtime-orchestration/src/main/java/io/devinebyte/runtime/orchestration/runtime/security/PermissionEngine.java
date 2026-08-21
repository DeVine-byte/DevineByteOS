package io.devinebyte.runtime.orchestration.runtime.security;

import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PermissionEngine {
    private final Set<Permission> permissions = ConcurrentHashMap.newKeySet();

    public boolean isAllowed(TenantContext ctx, String principal, String resource, String action) {
        return permissions.contains(new Permission(ctx.tenantId(), principal, resource, action));
    }

    public void grant(Permission p) { permissions.add(p); }
}
