package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;

public final class CompilerEngine {

    private final CompilerConfiguration configuration;

    public CompilerEngine(CompilerConfiguration configuration) {
        this.configuration = configuration;
    }

    public CompilationResult compile() {

        // Phase 2 pipeline
        //
        // Lexer
        // Parser
        // Semantic Analysis
        // Blueprint Compiler
        // Generator

        return new CompilationResult(
                true,
                "Compilation completed.",
                null
        );
    }

    public CompilerConfiguration configuration() {
        return configuration;
    }
}
