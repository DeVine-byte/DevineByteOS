package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;

public final class CompilerEngine {

    public CompilationResult compile() {
        return new CompilationResult(
                true,
                "Compilation completed.",
                null
        );
    }
}
