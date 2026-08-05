package io.devinebyte.runtime.projection.diagnostics;

import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;

@jakarta.inject.Singleton
public final class ProjectionDiagnostics {
    private final DiagnosticCollector collector = new DiagnosticCollector();
    public DiagnosticCollector collector() { return collector; }
    public void moduleDisabled(String tenantId, String moduleId) {
        collector.add("DBRT602", DiagnosticSeverity.WARN, "Projection skipped: module " + moduleId + " disabled for " + tenantId);
    }
}
