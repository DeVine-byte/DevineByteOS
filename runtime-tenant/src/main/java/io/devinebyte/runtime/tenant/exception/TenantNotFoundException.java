package io.devinebyte.runtime.tenant.exception;
public class TenantNotFoundException extends TenantException {
    public TenantNotFoundException(String code, String tenantId) { super(code, "Tenant not found: " + tenantId); }
}

