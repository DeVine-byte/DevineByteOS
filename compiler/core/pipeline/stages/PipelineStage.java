package compiler.core.pipeline.stages;

import compiler.core.context.CompilationContext;
import compiler.core.diagnostics.DiagnosticCollector;

public interface PipelineStage {

    Object process(Object input,
                   CompilationContext context,
                   DiagnosticCollector diagnostics);
}
