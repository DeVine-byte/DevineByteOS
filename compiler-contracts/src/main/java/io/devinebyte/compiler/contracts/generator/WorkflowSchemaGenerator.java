package io.devinebyte.compiler.contracts.generator;

import io.devinebyte.compiler.blueprint.model.BlueprintIR;
import io.devinebyte.compiler.contracts.model.WorkflowSchema;
import io.devinebyte.compiler.contracts.model.Step;
import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class WorkflowSchemaGenerator {
    
    public List<WorkflowSchema> generate(TenantContext tenant, BlueprintIR ir) {
        return ir.workflows().stream()
            .map(w -> new WorkflowSchema(w.name(), List.of()))
            .toList();
    }
}
