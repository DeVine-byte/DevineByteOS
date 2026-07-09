package org.devinebyte.sdk.diagnostics;

import java.util.Objects;

/**
 * Represents a warning diagnostic produced during compilation.
 *
 * Warning diagnostics indicate non-fatal issues that do not
 * prevent successful compilation but should be reviewed.
 */
public final class CompilerWarning implements Diagnostic {

    private final String code;
    private final String message;
    private final SourceLocation location;

    /**
     * Creates a compiler warning.
     *
     * @param code diagnostic code
     * @param message diagnostic message
     * @param location source location (may be null)
     */
    public CompilerWarning(
            String code,
            String message,
            SourceLocation location) {

        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.location = location;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public DiagnosticSeverity getSeverity() {
        return DiagnosticSeverity.WARNING;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        if (location == null) {
            return "[" + code + "] WARNING: " + message;
        }

        return "[" + code + "] WARNING: "
                + message
                + " ("
                + location
                + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CompilerWarning other)) {
            return false;
        }

        return code.equals(other.code)
                && message.equals(other.message)
                && Objects.equals(location, other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, location);
    }
}
