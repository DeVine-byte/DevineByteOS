package org.devinebyte.compiler.core.pipeline;

import org.devinebyte.compiler.api.CompilationResult;

public interface PipelineStage {

    CompilationResult execute(PipelineContext context);
}
