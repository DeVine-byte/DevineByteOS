package org.devinebyte.sdk.diagnostics;

import java.util.Objects;

/**
 * Represents an error diagnostic produced during compilation.
 *
 * Error diagnostics indicate conditions that prevent successful
 * compilation.
 */
public final class CompilerError implements Diagnostic {

    private final String code;
    private final String message;
    private final SourceLocation location;

    /**
     * Creates a compiler error.
     *
     * @param code diagnostic code
     * @param message diagnostic message
     * @param location source location (may be null)
     */
    public CompilerError(
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
        return DiagnosticSeverity.ERROR;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        if (location == null) {
            return "[" + code + "] ERROR: " + message;
        }

        return "[" + code + "] ERROR: "
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

        if (!(obj instanceof CompilerError other)) {
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
