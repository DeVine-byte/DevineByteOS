package io.devinebyte.compiler.dsl.type;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.ast.*;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Set;

@Singleton
public class TypeChecker {
    private static final Set<String> VALID_PRIMITIVES = Set.of(
        "STRING", "UUID", "LONG", "INT", "DECIMAL", "DOUBLE", "FLOAT", "BOOLEAN", "BOOL", "DATETIME", "DATE", "TIME", "JSON", "MAP"
    );

    public void check(CompilationContext context, List<AstNode> ast) {
        if (ast == null || ast.isEmpty()) {
            context.diagnostics().addInfo("TYPECHECK_SKIPPED", "No AST nodes provided for verification checking.");
            return;
        }

        String tenantId = context.tenant().tenantId();

        for (AstNode node : ast) {
            // Traverse recursively if nested inside a root-level module definition block
            if (node instanceof ModuleNode moduleNode) {
                if (moduleNode.children() != null) {
                    check(context, moduleNode.children());
                }
            } else if (node instanceof EntityNode entityNode) {
                validateEntity(context, entityNode, tenantId);
            } else if (node instanceof EventNode eventNode) {
                validateEvent(context, eventNode, tenantId);
            } else if (node instanceof WorkflowNode workflowNode) {
                validateWorkflow(context, workflowNode, tenantId);
            }
        }

        context.diagnostics().addInfo("TYPECHECK_COMPLETE", "Type checking finished");
    }

    private void validateEntity(CompilationContext context, EntityNode entity, String tenantId) {
        if (entity.fields() != null) {
            entity.fields().forEach((fieldName, typeStr) -> {
                if (typeStr == null || !VALID_PRIMITIVES.contains(typeStr.toUpperCase().trim())) {
                    context.diagnostics().addError("TYPE_001", 
                        String.format("Entity Validation Failure: Entity '%s' references unsupported property type definition: %s", entity.name(), typeStr), 
                        tenantId);
                }
            });
        }
    }

    private void validateEvent(CompilationContext context, EventNode event, String tenantId) {
        if (event.payload() != null) {
            event.payload().forEach((fieldName, typeStr) -> {
                if (typeStr == null || !VALID_PRIMITIVES.contains(typeStr.toUpperCase().trim())) {
                    context.diagnostics().addError("TYPE_002", 
                        String.format("Event Payload Failure: Event '%s' contains unmappable parameters type layout: %s", event.name(), typeStr), 
                        tenantId);
                }
            });
        }
    }

    private void validateWorkflow(CompilationContext context, WorkflowNode workflow, String tenantId) {
        if (workflow.steps() == null || workflow.steps().isEmpty()) {
            context.diagnostics().addError("TYPE_003", 
                String.format("Automation Constraint Violation: Workflow execution machine '%s' does not contain any functional sequential steps.", workflow.name()), 
                tenantId);
        }
    }
}

