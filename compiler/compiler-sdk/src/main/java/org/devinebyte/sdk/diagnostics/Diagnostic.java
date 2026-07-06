package org.devinebyte.compiler.api.diagnostics;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public interface Diagnostic {

    String getCode();

    String getMessage();

    DiagnosticSeverity getSeverity();

    SourceLocation getLocation();

}
