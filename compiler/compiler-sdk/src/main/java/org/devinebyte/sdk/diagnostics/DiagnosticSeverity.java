package org.devinebyte.sdk.diagnostics;

/**
 * Severity level of a diagnostic reported during compilation.
 *
 * <ul>
 *     <li>{@code ERROR} - Prevents successful compilation.</li>
 *     <li>{@code WARNING} - Indicates a potential issue but does not stop compilation.</li>
 *     <li>{@code INFO} - Provides informational messages.</li>
 * </ul>
 */
public enum DiagnosticSeverity {

    /**
     * Compilation cannot continue successfully.
     */
    ERROR,

    /**
     * A non-fatal issue that should be reviewed.
     */
    WARNING,

    /**
     * Informational diagnostic.
     */
    INFO;

    /**
     * Returns {@code true} if this severity represents an error.
     *
     * @return {@code true} when this severity is {@link #ERROR}
     */
    public boolean isError() {
        return this == ERROR;
    }

    /**
     * Returns {@code true} if this severity represents a warning.
     *
     * @return {@code true} when this severity is {@link #WARNING}
     */
    public boolean isWarning() {
        return this == WARNING;
    }

    /**
     * Returns {@code true} if this severity is informational.
     *
     * @return {@code true} when this severity is {@link #INFO}
     */
    public boolean isInfo() {
        return this == INFO;
    }
}
