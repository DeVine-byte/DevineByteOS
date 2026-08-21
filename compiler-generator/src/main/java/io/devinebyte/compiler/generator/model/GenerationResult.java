package io.devinebyte.compiler.generator.model;

import io.devinebyte.compiler.contracts.model.EventSchema;
import io.devinebyte.compiler.contracts.model.EntitySchema;
import io.devinebyte.compiler.contracts.model.WorkflowSchema;
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter; // CHANGED
import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema; // CHANGED
import io.devinebyte.runtime.config.ModuleGraph;
import java.util.List;
import java.util.Map;

public record GenerationResult(
    List<EventSchema> eventSchemas,
    List<EntitySchema> entitySchemas,
    List<WorkflowSchema> workflowSchemas,
    List<ApiSchema> apiSchemas, // CHANGED: was APISchema
    List<Object> workflows,      // ExecutableStateMachine
    List<Object> projections,    // ProjectionFunction
    List<Object> dashboards,     // DashboardDefinition
    Map<String, String> config,
    Map<String, Boolean> featureFlags,
    ModuleGraph moduleGraph
) {}
