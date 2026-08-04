package io.devinebyte.runtime.module.exception;
public class ModuleDisabledAccessException extends RuntimeException {
    public ModuleDisabledAccessException(String code, String message, String tenantId) {
        super("[" + code + "] " + message + " tenant=" + tenantId);
    }
}
