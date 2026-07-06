package org.devinebyte.compiler.api;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.sdk.CompilerContext;

public interface CompilerStage {

    String name();

    void execute(CompilerContext context);
}
