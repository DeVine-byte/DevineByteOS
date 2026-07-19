package org.devinebyte.compiler.blueprint.mapper;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.compiler.blueprint.model.EntityDefinition;
import org.devinebyte.compiler.blueprint.model.PropertyDefinition;
import org.devinebyte.compiler.blueprint.model.WorkflowDefinition;
import org.devinebyte.compiler.parser.ast.AstNode;
import org.devinebyte.compiler.parser.ast.EntityNode;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.ast.PropertyNode;
import org.devinebyte.compiler.parser.ast.WorkflowNode;

public final class AstToBlueprintMapper {

    public BlueprintModel map(ProgramNode program) {

        BlueprintModel model = new BlueprintModel();

        for (AstNode node : program.getDeclarations()) {

            if (node instanceof EntityNode entityNode) {

                EntityDefinition entity = new EntityDefinition();
                entity.setName(entityNode.getName());

                for (PropertyNode propertyNode : entityNode.getProperties()) {

                    PropertyDefinition property = new PropertyDefinition();

                    property.setName(propertyNode.getName());
                    property.setType(propertyNode.getType());

                    entity.getProperties().add(property);
                }

                model.getEntities().add(entity);
            }

            if (node instanceof WorkflowNode workflowNode) {

                WorkflowDefinition workflow = new WorkflowDefinition();
                workflow.setName(workflowNode.getName());

                model.getWorkflows().add(workflow);
            }
        }

        return model;
    }
}
