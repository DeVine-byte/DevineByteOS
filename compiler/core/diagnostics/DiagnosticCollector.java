package compiler.core.diagnostics;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticCollector {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void info(String code, String message) {
        diagnostics.add(new Diagnostic(Diagnostic.Level.INFO, code, message));
    }

    public void warn(String code, String message) {
        diagnostics.add(new Diagnostic(Diagnostic.Level.WARNING, code, message));
    }

    public void error(String code, String message) {
        diagnostics.add(new Diagnostic(Diagnostic.Level.ERROR, code, message));
    }

    public boolean hasErrors() {
        return diagnostics.stream()
                .anyMatch(d -> d.getLevel() == Diagnostic.Level.ERROR);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
