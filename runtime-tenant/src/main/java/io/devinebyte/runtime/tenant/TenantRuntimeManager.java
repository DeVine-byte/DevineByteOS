package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.bootstrap.BootstrapResult;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.tenant.exception.TenantLifecycleException; // ADD THIS
import io.devinebyte.runtime.tenant.lifecycle.TenantLifecycleController;
import io.devinebyte.runtime.tenant.registry.TenantRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TenantRuntimeManager {
    private final TenantRuntimeFactory factory;
    private final TenantRegistry registry;
    private final RuntimeRegistry runtimeRegistry;
    private final TenantLifecycleController lifecycleController;

    @Inject
    public TenantRuntimeManager(
        TenantRuntimeFactory factory,
        TenantRegistry registry,
        RuntimeRegistry runtimeRegistry,
        TenantLifecycleController lifecycleController
    ) {
        this.factory = factory;
        this.registry = registry;
        this.runtimeRegistry = runtimeRegistry;
        this.lifecycleController = lifecycleController;
    }

    public TenantRuntime createTenant(TenantContext tenant, BootstrapResult bootstrap) {
        if (!bootstrap.success()) {
            throw new TenantLifecycleException("DBRT009", "Bootstrap failed"); // now compiles
        }
        TenantRuntime runtime = factory.create(tenant, bootstrap);
        registry.register(tenant.tenantId(), runtime);
        
        TenantContext active = lifecycleController.transition(runtime.tenantContext(), TenantLifecycle.ACTIVE);
        return new TenantRuntime(active, runtime.config(), runtime.moduleGraph(), runtime.featureFlags(), runtime.dbpkgPath());
    }

    public void destroyTenant(TenantContext tenant) {
        TenantRuntime runtime = registry.get(tenant);
        TenantContext decomm = lifecycleController.transition(runtime.tenantContext(), TenantLifecycle.DECOMMISSIONED);
        registry.unregister(tenant.tenantId());
    }
}
