package org.devinebyte.compiler.blueprint.model;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;

public class BlueprintModel {

    private final List<EntityDefinition> entities = new ArrayList<>();

    private final List<WorkflowDefinition> workflows = new ArrayList<>();

    public List<EntityDefinition> getEntities() {
        return entities;
    }

    public List<WorkflowDefinition> getWorkflows() {
        return workflows;
    }

}
