package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;
import org.devinebyte.compiler.core.pipeline.CompilationPipeline;
import org.devinebyte.compiler.core.pipeline.PipelineContext;
import org.devinebyte.compiler.core.pipeline.ProjectLoaderStage;

import java.util.List;

public final class CompilerEngine {

    private final CompilerConfiguration configuration;
    private final CompilationPipeline pipeline;

    public CompilerEngine(CompilerConfiguration configuration) {

        this.configuration = configuration;

        this.pipeline = new CompilationPipeline(
                List.of(
                        new ProjectLoaderStage()
                )
        );
    }

    public CompilationResult compile() {

        return pipeline.execute(
                new PipelineContext(configuration)
        );
    }
}
