package org.devinebyte.sdk.diagnostics;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public final class CompilerWarning implements Diagnostic {

    private final String code;
    private final String message;
    private final SourceLocation location;

    public CompilerWarning(
            String code,
            String message,
            SourceLocation location) {

        this.code = code;
        this.message = message;
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
        return DiagnosticDiagnosticSeverity.WARNING;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

}
