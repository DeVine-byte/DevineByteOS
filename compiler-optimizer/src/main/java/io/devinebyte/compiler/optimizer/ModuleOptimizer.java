package io.devinebyte.compiler.optimizer;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ModuleOptimizer {
    public BlueprintIR optimize(CompilationContext context, BlueprintIR ir) {
        context.diagnostics().addInfo("OPT_MODULE", "Removing disabled modules");

        List<ModuleIR> enabledModules = ir.modules().stream()
            .filter(ModuleIR::enabled)
            .toList();

        if (enabledModules.size() != ir.modules().size()) {
            context.diagnostics().addInfo("OPT_MODULE", "Removed " + (ir.modules().size() - enabledModules.size()) + " disabled modules");
        }

        return new BlueprintIR(
            ir.tenantId(),
            ir.version(),
            ir.enabledModules(), // ADD THIS
            enabledModules,
            ir.entities(),
            ir.events(),
            ir.workflows(),
            ir.kpiFormulas() // ADD THIS
        );
    }
}
