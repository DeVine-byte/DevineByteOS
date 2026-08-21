package io.devinebyte.compiler.optimizer;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EntityIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.optimizer.util.DeterministicSorter;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ProjectionOptimizer {
    public BlueprintIR optimize(CompilationContext context, BlueprintIR ir) {
        context.diagnostics().addInfo("OPT_PROJECTION", "Sorting entities deterministically for projection stability");

        List<EntityIR> sortedEntities = DeterministicSorter.sortByName(
            ir.entities().stream(),
            EntityIR::name
        );

        return new BlueprintIR(
            ir.tenantId(),
            ir.version(),
            ir.enabledModules(),
            ir.modules(),
            sortedEntities,
            ir.events(),
            ir.workflows(),
            ir.kpiFormulas(),
            ir.apiSchemas() // ADD 9TH ARG
        );
    }
}
