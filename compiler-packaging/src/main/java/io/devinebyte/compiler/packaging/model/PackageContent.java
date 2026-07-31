package io.devinebyte.compiler.packaging.model;

import io.devinebyte.compiler.core.context.TenantContext;
import io.devinebyte.compiler.contracts.model.*;
import io.devinebyte.compiler.workflow.model.ExecutableStateMachine;
import io.devinebyte.compiler.projection.model.DashboardDefinition;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import io.devinebyte.runtime.config.ModuleGraph;

import java.util.List;
import java.util.Map;

public record PackageContent(
    TenantContext tenant,
    String version,
    List<EventSchema> eventSchemas,
    List<EntitySchema> entitySchemas,
    List<WorkflowSchema> workflowSchemas,
    List<APISchema> apiSchemas,
    List<ExecutableStateMachine> workflows,
    List<ProjectionFunction> projections,
    List<DashboardDefinition> dashboards,
    byte[] runtimeBootstrapClass,
    Map<String, String> tenantConfig,
    Map<String, Boolean> featureFlags,
    ModuleGraph moduleGraph,
    boolean multiTenant // NEW: HYBRID MODE FLAG
) {}
