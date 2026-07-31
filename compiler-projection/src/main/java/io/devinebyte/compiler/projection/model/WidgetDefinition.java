package io.devinebyte.compiler.projection.model;

public record WidgetDefinition(
    String id,
    String type, // "table", "kpi", "chart"
    String title,
    String projectionName,
    String query // JSON query DSL that runs against projection
) {}
