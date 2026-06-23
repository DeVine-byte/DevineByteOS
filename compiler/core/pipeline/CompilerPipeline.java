package compiler.core.pipeline;

import compiler.core.context.CompilationContext;
import compiler.core.diagnostics.DiagnosticCollector;
import compiler.core.pipeline.stages.PipelineStage;
import compiler.core.bootstrap.CompilationResult;

import java.util.List;

public class CompilerPipeline {

    private final List<PipelineStage> stages;

    public CompilerPipeline(List<PipelineStage> stages) {
        this.stages = stages;
    }

    public CompilationResult execute(CompilationContext context,
                                     DiagnosticCollector diagnostics) {

        Object input = context.getSource();

        for (PipelineStage stage : stages) {
            input = stage.process(input, context, diagnostics);

            if (diagnostics.hasErrors()) {
                return CompilationResult.failed(diagnostics.getDiagnostics());
            }
        }

        return CompilationResult.success(input);
    }
}
