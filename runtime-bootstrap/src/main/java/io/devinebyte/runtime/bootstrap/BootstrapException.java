package io.devinebyte.runtime.bootstrap;

/**
 * Runtime exception wrapper. We still log via DiagnosticCollector first.
 */
public class BootstrapException extends RuntimeException {
    public BootstrapException(String message) {
        super(message);
    }
}
