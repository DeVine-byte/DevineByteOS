package io.devinebyte.runtime.core.diagnostics;

import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DiagnosticCollector {
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void add(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public void error(String code, String message, String tenantId) {
        add(new Diagnostic(code, DiagnosticSeverity.ERROR, message, tenantId, null));
    }

    public void fatal(String code, String message, String tenantId) {
        add(new Diagnostic(code, DiagnosticSeverity.FATAL, message, tenantId, null));
    }

    public List<Diagnostic> getAll() {
        return List.copyOf(diagnostics);
    }

    public boolean hasFatal() {
        return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.FATAL);
    }
}
