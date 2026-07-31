package io.devinebyte.compiler.core.diagnostics;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticCollector {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void addError(String code, String message) {
        diagnostics.add(new Diagnostic(DiagnosticSeverity.ERROR, code, message));
    }
    
    public void addError(String code, String message, String phase) {
        diagnostics.add(new Diagnostic(DiagnosticSeverity.ERROR, code, message, phase));
    }
    
    public void addWarning(String code, String message) {
        diagnostics.add(new Diagnostic(DiagnosticSeverity.WARNING, code, message));
    }
    
    public void addInfo(String code, String message) {
        diagnostics.add(new Diagnostic(DiagnosticSeverity.INFO, code, message));
    }
    
    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
    }
   public void addFatal(String code, String message) {
        diagnostics.add(new Diagnostic(DiagnosticSeverity.FATAL, code, message));
    }
    
    public List<Diagnostic> getDiagnostics() { return List.copyOf(diagnostics); }
}
