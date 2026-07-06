package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.CompilerContext;

public interface CompilerStage {

    String name();

    void execute(CompilerContext context);
}
