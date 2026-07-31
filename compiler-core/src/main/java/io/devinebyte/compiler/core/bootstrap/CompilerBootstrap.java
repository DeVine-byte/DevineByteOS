package io.devinebyte.compiler.core.bootstrap;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPipeline;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import jakarta.inject.Singleton;

@Singleton
public class CompilerBootstrap {
    private final CompilerPipeline pipeline;

    public CompilerBootstrap(CompilerPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public CompilerResult bootstrap(CompilationContext context) {
        // pipeline now owns the loop and starts with CompilerResult.empty(ctx)
        return pipeline.run(context); // <-- remove initialInput
    }
}
