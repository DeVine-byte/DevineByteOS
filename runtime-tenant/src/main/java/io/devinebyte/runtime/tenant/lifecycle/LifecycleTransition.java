package io.devinebyte.runtime.tenant.lifecycle;

import java.time.Instant;

public record LifecycleTransition(String tenantId, io.devinebyte.runtime.core.context.TenantLifecycle from, io.devinebyte.runtime.core.context.TenantLifecycle to, Instant occurredAt) {}
