package io.devinebyte.compiler.core.pipeline;

import io.devinebyte.compiler.core.context.CompilationContext;

public interface CompilerPhase {
    String name();
    CompilerResult execute(CompilationContext ctx, CompilerResult input) throws Exception;
}
