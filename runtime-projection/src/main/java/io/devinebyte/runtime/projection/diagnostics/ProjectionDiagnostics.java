package io.devinebyte.runtime.projection.diagnostics; 

import io.devinebyte.runtime.core.diagnostics.Diagnostic;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;

import java.time.Instant;

public record ProjectionDiagnostics(DiagnosticCollector collector) {
    public void moduleDisabled(String tenantId, String moduleId) {
        collector.add(new Diagnostic(
            "DBRT603", 
            DiagnosticSeverity.WARNING, 
            "Module " + moduleId + " disabled for " + tenantId, 
            tenantId, 
            Instant.now()
        ));
    }
}
