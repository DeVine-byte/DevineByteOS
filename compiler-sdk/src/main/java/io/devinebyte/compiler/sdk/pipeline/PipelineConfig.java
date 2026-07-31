package io.devinebyte.compiler.sdk.pipeline;

import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerPipeline;
import jakarta.inject.Singleton;
import jakarta.inject.Provider;
import java.util.List;

@Singleton
public class PipelineConfig {

    @Singleton
    public CompilerPipeline compilerPipeline(
        Provider<io.devinebyte.compiler.audit.phase.AuditPhase> audit, // 1
        Provider<io.devinebyte.compiler.dsl.phase.DslPhase> dsl,      // 2  
        Provider<io.devinebyte.compiler.blueprint.compiler.BlueprintCompiler> blueprint, // 3
        Provider<io.devinebyte.compiler.optimizer.OptimizerPhase> optimizer, // 4
        Provider<io.devinebyte.compiler.contracts.phase.ContractsPhase> contracts, // 5
        Provider<io.devinebyte.compiler.workflow.phase.WorkflowPhase> workflow, // 6
        Provider<io.devinebyte.compiler.projection.phase.ProjectionPhase> projection, // 7
        Provider<io.devinebyte.compiler.generator.phase.GeneratorPhase> generator, // 8
        Provider<io.devinebyte.compiler.packaging.phase.PackagingPhase> packaging, // 9
        Provider<io.devinebyte.compiler.reporting.phase.ReportingPhase> reporting // 10
    ) {
        List<CompilerPhase> phases = List.of(
            audit.get(), dsl.get(), blueprint.get(), optimizer.get(), contracts.get(),
            workflow.get(), projection.get(), generator.get(), packaging.get(), reporting.get()
        );
        return new CompilerPipeline(phases);
    }
}
