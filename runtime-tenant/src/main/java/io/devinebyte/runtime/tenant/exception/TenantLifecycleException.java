package io.devinebyte.runtime.tenant.exception;
public class TenantLifecycleException extends TenantException {
    public TenantLifecycleException(String code, String message) { super(code, message); }
    public TenantLifecycleException(String code, Throwable cause) { super(code, cause.getMessage()); }
}
