package io.devinebyte.compiler.blueprint.model;

import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema; // IMPORT THE RECORD
import java.util.List;
import java.util.Set;

public record BlueprintIR(
    String tenantId,
    String version,
    Set<String> enabledModules,
    List<ModuleIR> modules,
    List<EntityIR> entities,
    List<EventIR> events,
    List<WorkflowIR> workflows,
    List<String> kpiFormulas,
    List<ApiSchema> apiSchemas // use imported type
) {}
