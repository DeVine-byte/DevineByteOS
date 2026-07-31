package io.devinebyte.runtime.tenant.lifecycle;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.context.TenantLifecycle;
import io.devinebyte.runtime.tenant.exception.TenantLifecycleException;
import jakarta.inject.Singleton;

@Singleton
public class TenantLifecycleController {

    public TenantContext transition(TenantContext tenant, TenantLifecycle newState) {
        if (!LifecycleTransition.isValid(tenant.state(), newState)) {
            throw new TenantLifecycleException("DBRT005", 
                "Invalid transition " + tenant.state() + " -> " + newState);
        }
        return new TenantContext(tenant.tenantId(), newState, tenant.enabledModules());
    }
}
