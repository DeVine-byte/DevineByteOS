package org.devinebyte.compiler.api;

import org.devinebyte.compiler.core.CompilationContext;

public interface CompilerStage {

    String name();

    void execute(CompilationContext context);
}
