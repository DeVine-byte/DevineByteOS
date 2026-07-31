package io.devinebyte.compiler.optimizer;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.WorkflowIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.optimizer.util.DeterministicSorter;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class WorkflowOptimizer {
    public BlueprintIR optimize(CompilationContext context, BlueprintIR ir) {
        context.diagnostics().addInfo("OPT_WORKFLOW", "Sorting workflows deterministically");

        List<WorkflowIR> sortedWorkflows = DeterministicSorter.sortByName(
            ir.workflows().stream(),
            WorkflowIR::name
        );

        return new BlueprintIR(
            ir.tenantId(),
            ir.version(),
            ir.enabledModules(), // ADD
            ir.modules(),
            ir.entities(),
            ir.events(),
            sortedWorkflows,
            ir.kpiFormulas() // ADD
        );
    }
}
