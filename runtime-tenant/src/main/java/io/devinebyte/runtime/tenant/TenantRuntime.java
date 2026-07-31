package io.devinebyte.runtime.tenant;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.config.TenantConfig;
import io.devinebyte.runtime.config.FeatureFlags;
import java.nio.file.Path;

/**
 * Immutable handle to a live tenant. No business logic here. Just state.
 */
public record TenantRuntime(
    TenantContext tenantContext,
    TenantConfig config,
    ModuleGraph moduleGraph,
    FeatureFlags featureFlags,
    Path dbpkgPath
) {
    public boolean isModuleEnabled(String moduleId) {
        return tenantContext.isModuleEnabled(moduleId);
    }
}
