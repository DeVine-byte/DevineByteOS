package org.devinebyte.sdk.diagnostics;
import org.devinebyte.compiler.api.DiagnosticSeverity;

public final class CompilerError implements Diagnostic {

    private final String code;
    private final String message;
    private final SourceLocation location;

    public CompilerError(
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
        return DiagnosticSeverity.ERROR;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

}
