package io.devinebyte.compiler.blueprint.shake;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EntityIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.blueprint.model.ModuleIR;
import io.devinebyte.compiler.blueprint.model.WorkflowIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import jakarta.inject.Singleton;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ModuleTreeShaker implements CompilerPhase {

    @Override
    public String name() {
        return "compiler-blueprint-treeshaker";
    }

    @Override
    public CompilerResult execute(CompilationContext context, CompilerResult input) {

        BlueprintIR rawIR = (BlueprintIR) input.output();

        Set<String> enabled = context.tenant()
                .enabledModules()
                .stream()
                .map(this::canonical)
                .collect(Collectors.toSet());

        context.diagnostics().addInfo("TREE_SHAKE", "Enabled modules: " + enabled);

        var keptModules = rawIR.modules().stream()
                .filter(m -> enabled.contains(canonical(m.id())))
                .map(m -> new ModuleIR(
                        canonical(m.id()),
                        m.name(),
                        true,
                        m.dependencies(),
                        m.entities(),
                        m.events(),
                        m.workflows()
                ))
                .toList();

        var keptEntities = rawIR.entities().stream()
                .filter(e -> enabled.contains(canonical(e.moduleId())))
                .toList();

        var keptEvents = rawIR.events().stream()
                .filter(e -> enabled.contains(canonical(e.moduleId())))
                .toList();

        var keptWorkflows = rawIR.workflows().stream()
                .filter(w -> enabled.contains(canonical(w.moduleId())))
                .toList();

        if (keptModules.size() != rawIR.modules().size()) {
            int removed = rawIR.modules().size() - keptModules.size();
            context.diagnostics().addInfo("TREE_SHAKE", "Shaken out " + removed + " disabled modules");
        }

        BlueprintIR shakenIR = new BlueprintIR(
                rawIR.tenantId(),
                rawIR.version(),
                enabled,
                keptModules,
                keptEntities,
                keptEvents,
                keptWorkflows,
                rawIR.kpiFormulas(),
                rawIR.apiSchemas() // ADD 9TH ARG
        );

        context.put("blueprint", shakenIR);

        return new CompilerResult<>(
                context.tenant(),
                context.diagnostics(),
                shakenIR
        );
    }

    private String canonical(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
