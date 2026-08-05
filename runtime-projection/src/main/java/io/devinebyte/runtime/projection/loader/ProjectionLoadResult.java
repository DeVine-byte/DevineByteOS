package io.devinebyte.runtime.projection.loader;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.compiler.projection.model.DashboardDefinition;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import java.util.List;

public record ProjectionLoadResult(
    List<ProjectionFunction> functions,
    List<DashboardDefinition> dashboards,
    DiagnosticCollector diagnostics
) {}
