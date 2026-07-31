package io.devinebyte.runtime.tenant.exception;
public class TenantException extends RuntimeException {
    public final String code;
    public TenantException(String code, String message) { super(message); this.code = code; }
}
