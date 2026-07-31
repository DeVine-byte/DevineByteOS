package io.devinebyte.compiler.generator.model;

import io.devinebyte.compiler.contracts.model.*;
import io.devinebyte.runtime.config.ModuleGraph; // ADD
import java.util.List;
import java.util.Map;

public record GenerationResult(
    List<EventSchema> eventSchemas,
    List<EntitySchema> entitySchemas,
    List<WorkflowSchema> workflowSchemas,
    List<APISchema> apiSchemas,
    List<Object> workflows,      // ExecutableStateMachine
    List<Object> projections,    // ProjectionFunction
    List<Object> dashboards,     // DashboardDefinition
    Map<String, String> config,
    Map<String, Boolean> featureFlags,
    ModuleGraph moduleGraph
) {}
