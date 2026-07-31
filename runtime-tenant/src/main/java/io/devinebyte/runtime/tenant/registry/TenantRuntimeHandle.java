package io.devinebyte.runtime.tenant.registry;

import io.devinebyte.runtime.tenant.TenantRuntime;

/**
 * Wrapper for future: ref counting, shutdown hooks
 */
public record TenantRuntimeHandle(TenantRuntime runtime) {}
