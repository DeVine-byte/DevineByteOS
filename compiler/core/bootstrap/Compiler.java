package compiler.core.bootstrap;

import compiler.core.context.CompilationContext;
import compiler.core.pipeline.CompilerPipeline;
import compiler.core.diagnostics.DiagnosticCollector;

public class Compiler {

    private final CompilerPipeline pipeline;
    private final DiagnosticCollector diagnostics;

    public Compiler(CompilerPipeline pipeline, DiagnosticCollector diagnostics) {
        this.pipeline = pipeline;
        this.diagnostics = diagnostics;
    }

    public CompilationResult compile(CompilationContext context) {
        try {
            return pipeline.execute(context, diagnostics);
        } catch (Exception e) {
            diagnostics.error("COMPILER_FATAL", e.getMessage());
            return CompilationResult.failed(diagnostics.getDiagnostics());
        }
    }
}
