package io.devinebyte.compiler.blueprint.model;

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
    List<String> kpiFormulas
) {}
