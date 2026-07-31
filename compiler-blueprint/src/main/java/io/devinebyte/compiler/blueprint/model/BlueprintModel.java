package io.devinebyte.compiler.blueprint.model;

import io.devinebyte.compiler.audit.model.*;
import io.devinebyte.compiler.core.context.TenantLifecycle;
import java.util.List;
import java.util.Set;

public record BlueprintModel(
    String tenantId,
    String companyName,
    TenantLifecycle targetLifecycle,
    Set<String> enabledModules,
    List<BusinessUnit> businessUnits,
    List<ProcessDefinition> processes,
    List<KpiDefinition> kpis,
    List<Recommendation> recommendations
) {}
