package io.devinebyte.compiler.workflow.phase;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase; import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.workflow.compiler.WorkflowCompiler;
import jakarta.inject.Inject; import jakarta.inject.Singleton;

@Singleton
public class WorkflowPhase implements CompilerPhase {
    private final WorkflowCompiler compiler;
    @Inject public WorkflowPhase(WorkflowCompiler compiler) { this.compiler = compiler; }
    @Override public String name() { return "workflow"; }
    @Override public CompilerResult execute(CompilationContext context, CompilerResult input) {
        BlueprintIR ir = (BlueprintIR) input.output();
        compiler.compile(context.tenant(), context, ir);
        return new CompilerResult<>(context.tenant(), context.diagnostics(), ir);
    }
}
