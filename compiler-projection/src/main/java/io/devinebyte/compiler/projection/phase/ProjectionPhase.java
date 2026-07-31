package io.devinebyte.compiler.projection.phase;
import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase; import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.projection.compiler.ProjectionCompiler;
import jakarta.inject.Inject; import jakarta.inject.Singleton;

@Singleton
public class ProjectionPhase implements CompilerPhase {
    private final ProjectionCompiler compiler;
    @Inject public ProjectionPhase(ProjectionCompiler compiler) { this.compiler = compiler; }
    @Override public String name() { return "projection"; }
    @Override public CompilerResult execute(CompilationContext context, CompilerResult input) {
        BlueprintIR ir = (BlueprintIR) input.output();
        compiler.compile(context.tenant(), context, ir);
        return new CompilerResult<>(context.tenant(), context.diagnostics(), ir);
    }
}
