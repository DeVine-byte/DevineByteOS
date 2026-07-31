package io.devinebyte.compiler.optimizer;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import jakarta.inject.Inject; import jakarta.inject.Singleton;

@Singleton
public class OptimizerPhase implements CompilerPhase {
    private final ModuleOptimizer moduleOptimizer;
    private final WorkflowOptimizer workflowOptimizer;
    private final ProjectionOptimizer projectionOptimizer;

    @Inject public OptimizerPhase(ModuleOptimizer moduleOptimizer, WorkflowOptimizer workflowOptimizer, ProjectionOptimizer projectionOptimizer) {
        this.moduleOptimizer = moduleOptimizer; this.workflowOptimizer = workflowOptimizer; this.projectionOptimizer = projectionOptimizer;
    }

    @Override public String name() { return "optimizer"; }

    @Override
    public CompilerResult execute(CompilationContext context, CompilerResult input) {
        BlueprintIR ir = (BlueprintIR) input.output();
        context.diagnostics().addInfo("OPTIMIZER_START", "Starting deterministic optimization");
        BlueprintIR step1 = moduleOptimizer.optimize(context, ir);
        BlueprintIR step2 = workflowOptimizer.optimize(context, step1);
        BlueprintIR step3 = projectionOptimizer.optimize(context, step2);
        context.diagnostics().addInfo("OPTIMIZER_END", "Optimization complete. IR is now deterministic");
        return new CompilerResult<>(context.tenant(), context.diagnostics(), step3);
    }
}
