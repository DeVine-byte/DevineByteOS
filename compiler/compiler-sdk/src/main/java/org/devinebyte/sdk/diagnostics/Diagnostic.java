package org.devinebyte.sdk.diagnostics;
import org.devinebyte.compiler.api.DiagnosticSeverity;

public interface Diagnostic {

    String getCode();

    String getMessage();

    DiagnosticSeverity getSeverity();

    SourceLocation getLocation();

}
