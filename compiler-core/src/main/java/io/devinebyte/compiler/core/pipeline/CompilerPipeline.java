package io.devinebyte.compiler.core.pipeline;

import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class CompilerPipeline {
    private final List<CompilerPhase> phases;

    @Inject
    public CompilerPipeline(List<CompilerPhase> phases) { 
        this.phases = phases; 
    }

    public CompilerResult run(CompilationContext ctx) {
        CompilerResult result = CompilerResult.empty(ctx);
        for (CompilerPhase phase : phases) {
            try {
                result = phase.execute(ctx, result);
            } catch (Exception e) {
                ctx.diagnostics().addError("PHASE_FAILED", "Phase " + phase.name() + " failed: " + e.getMessage());
                return result; // stop pipeline on first failure
            }
        }
        return result;
    }
}
