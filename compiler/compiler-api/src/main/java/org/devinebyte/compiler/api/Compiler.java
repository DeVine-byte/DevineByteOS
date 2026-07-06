package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public interface Compiler {

    CompilationResult compile();
}
