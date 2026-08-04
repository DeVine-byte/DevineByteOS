
package io.devinebyte.runtime.module.exception;
public class ModuleDependencyCycleException extends RuntimeException {
    public ModuleDependencyCycleException(String message) { super(message); }
}

