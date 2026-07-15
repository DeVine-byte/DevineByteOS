package org.devinebyte.compiler.core.pipeline;

import org.devinebyte.compiler.api.CompilationResult;

import java.util.List;

public final class CompilationPipeline {

    private final List<PipelineStage> stages;

    public CompilationPipeline(List<PipelineStage> stages) {
        this.stages = List.copyOf(stages);
    }

    public CompilationResult execute(PipelineContext context) {

        CompilationResult result = null;

        for (PipelineStage stage : stages) {

            result = stage.execute(context);

            if (!result.success()) {
                return result;
            }
        }

        return result;
    }
}
