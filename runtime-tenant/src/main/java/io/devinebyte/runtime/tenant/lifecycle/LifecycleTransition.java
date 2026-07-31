package io.devinebyte.runtime.tenant.lifecycle;

import io.devinebyte.runtime.core.context.TenantLifecycle;
import java.util.Set;

public final class LifecycleTransition {
    private static final Set<String> VALID = Set.of(
        "PROVISIONING->ACTIVE",
        "ACTIVE->SUSPENDED", 
        "SUSPENDED->ACTIVE",
        "ACTIVE->DECOMMISSIONED",
        "SUSPENDED->DECOMMISSIONED"
    );

    public static boolean isValid(TenantLifecycle from, TenantLifecycle to) {
        return VALID.contains(from.name() + "->" + to.name());
    }
}
