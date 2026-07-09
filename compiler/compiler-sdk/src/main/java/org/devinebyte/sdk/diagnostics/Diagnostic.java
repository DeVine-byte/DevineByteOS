package org.devinebyte.sdk.diagnostics;

/**
 * Represents a diagnostic produced by the SDK.
 *
 * A diagnostic describes an issue or informational message
 * associated with a location in the user's source code.
 */
public interface Diagnostic {

    /**
     * Unique diagnostic code.
     *
     * @return diagnostic code
     */
    String getCode();

    /**
     * Human-readable diagnostic message.
     *
     * @return diagnostic message
     */
    String getMessage();

    /**
     * Severity of this diagnostic.
     *
     * @return diagnostic severity
     */
    DiagnosticSeverity getSeverity();

    /**
     * Source location where the diagnostic occurred.
     *
     * May return {@code null} if the diagnostic is not tied to
     * a specific source location.
     *
     * @return source location or {@code null}
     */
    SourceLocation getLocation();

    /**
     * Returns whether this diagnostic represents an error.
     *
     * @return true if severity is ERROR
     */
    default boolean isError() {
        return getSeverity() == DiagnosticSeverity.ERROR;
    }

    /**
     * Returns whether this diagnostic represents a warning.
     *
     * @return true if severity is WARNING
     */
    default boolean isWarning() {
        return getSeverity() == DiagnosticSeverity.WARNING;
    }

    /**
     * Returns whether this diagnostic is informational.
     *
     * @return true if severity is INFO
     */
    default boolean isInfo() {
        return getSeverity() == DiagnosticSeverity.INFO;
    }
}
