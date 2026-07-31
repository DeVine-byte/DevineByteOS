package io.devinebyte.compiler.audit.model;

import io.devinebyte.compiler.core.context.TenantLifecycle;
import java.util.List;
import java.util.Map;

public record AuditModel(
    String source, // ADD THIS
    String version, // ADD THIS
    String companyName,
    TenantLifecycle targetLifecycle,
    List<BusinessUnit> businessUnits,
    List<ProcessDefinition> processes,
    List<KpiDefinition> kpis,
    List<GapFinding> gaps,
    List<RiskFinding> risks,
    List<Recommendation> recommendations,
    Map<String, String> metadata // ADD THIS
) {}
