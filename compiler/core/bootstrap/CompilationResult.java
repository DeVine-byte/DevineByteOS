package compiler.core.bootstrap;

import java.util.List;
import compiler.core.diagnostics.Diagnostic;

public class CompilationResult {

    private final boolean success;
    private final List<Diagnostic> diagnostics;
    private final Object artifact;

    private CompilationResult(boolean success, List<Diagnostic> diagnostics, Object artifact) {
        this.success = success;
        this.diagnostics = diagnostics;
        this.artifact = artifact;
    }

    public static CompilationResult success(Object artifact) {
        return new CompilationResult(true, List.of(), artifact);
    }

    public static CompilationResult failed(List<Diagnostic> diagnostics) {
        return new CompilationResult(false, diagnostics, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getArtifact() {
        return artifact;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
