package org.devinebyte.compiler.core.pipeline;

import org.devinebyte.compiler.core.CompilerConfiguration;

public final class PipelineContext {

    private final CompilerConfiguration configuration;

    public PipelineContext(CompilerConfiguration configuration) {
        this.configuration = configuration;
    }

    public CompilerConfiguration configuration() {
        return configuration;
    }
}
