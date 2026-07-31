package io.devinebyte.compiler.projection.model;

import java.util.List;

public record DashboardDefinition(
    String dashboardId,
    String name,
    String module,
    List<WidgetDefinition> widgets
) {}

