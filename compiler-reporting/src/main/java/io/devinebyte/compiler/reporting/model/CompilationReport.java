package io.devinebyte.compiler.reporting.model;

import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public record CompilationReport(
    String tenantId,
    String version,
    boolean success,
    List<PhaseTiming> phaseTimings,
    DependencyGraph dependencyGraph,
    Map<String, Integer> moduleCounts,
    List<String> warnings,
    List<String> errors,
    String dbpkgPath
) {
    public static CompilationReport empty(TenantContext tenant) {
        return new CompilationReport(
            tenant.tenantId(), "0.0.0", false, List.of(), 
            DependencyGraph.empty(), Map.of(), List.of(), List.of(), ""
        );
    }
}
