package org.devinebyte.sdk.diagnostics;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public interface Diagnostic {

    String getCode();

    String getMessage();

    DiagnosticSeverity getSeverity();

    SourceLocation getLocation();

}
