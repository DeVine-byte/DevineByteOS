package org.devinebyte.compiler.core.pipeline;

import org.devinebyte.compiler.api.CompilationResult;

public final class ProjectLoaderStage implements PipelineStage {

    @Override
    public CompilationResult execute(PipelineContext context) {

        return new CompilationResult(
                true,
                "Project loaded.",
                null
        );
    }
}
