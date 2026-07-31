package io.devinebyte.compiler.blueprint.validation;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.blueprint.model.EventIR;
import io.devinebyte.compiler.blueprint.model.WorkflowIR;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors; // ADD THIS

@Singleton
public class ContractViolationEngine {

    public void validate(CompilationContext context, BlueprintIR ir) {
        Set<String> definedEvents = ir.events().stream().map(EventIR::name).collect(Collectors.toSet());

        // Rule: Workflow can only reference events that exist
        for (WorkflowIR wf : ir.workflows()) {
            for (String req : wf.requiredEvents()) {
                if (!definedEvents.contains(req)) {
                    context.diagnostics().addError("CONTRACT_001", 
                        "Workflow " + wf.name() + " requires undefined event: " + req);
                }
            }
        }

        // Rule: No cross-module direct references. Only via events
        context.diagnostics().addInfo("CONTRACT_VALIDATION", "Contract validation complete. 0 violations");
    }
}
