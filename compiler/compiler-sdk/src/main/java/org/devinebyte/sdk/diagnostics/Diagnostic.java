package org.devinebyte.sdk.diagnostics;

public interface Diagnostic {

    String getCode();

    String getMessage();

    DiagnosticSeverity getSeverity();

    SourceLocation getLocation();

}
