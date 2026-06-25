package org.devinebyte.compiler.diagnostics;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticCollector {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void add(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public List<Diagnostic> getAll() {
        return diagnostics;
    }
}
