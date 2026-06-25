package org.devinebyte.compiler.api;

import com.devinebyte.compiler.core.CompilationContext;

public interface CompilerStage {

    String name();

    void execute(CompilationContext context);
}
