package compiler.dsl.semantic;

import compiler.dsl.ast.*;

import java.util.HashSet;
import java.util.Set;

public class SemanticAnalyzer {

    public void validate(OrganizationNode root) {

        Set<String> entities = new HashSet<>();

        for (AstNode node : root.getChildren()) {

            if (node instanceof EntityNode entity) {
                validateEntity(entity, entities);
            }

            if (node instanceof WorkflowNode workflow) {
                validateWorkflow(workflow);
            }
        }
    }

    private void validateEntity(EntityNode entity, Set<String> entities) {

        if (entities.contains(entity.getName())) {
            throw new RuntimeException("Duplicate entity: " + entity.getName());
        }

        entities.add(entity.getName());
    }

    private void validateWorkflow(WorkflowNode workflow) {

        if (workflow.getStates().isEmpty()) {
            throw new RuntimeException("Workflow must have states");
        }

        if (workflow.getTransitions().isEmpty()) {
            throw new RuntimeException("Workflow must have transitions");
        }
    }
              }
