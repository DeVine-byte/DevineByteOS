package io.devinebyte.runtime.orchestration.runtime.security;

public record Permission(
    String tenantId,
    String principal,
    String resource,
    String action
) {}
