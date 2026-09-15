package io.devinebyte.runtime.core.observability;

import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public final class PlatformObservabilityEngine {

    /**
     * Emits a standardized production OpenTelemetry metric snapshot block.
     * Records exactly: dbos_boot_duration_seconds and dbos_module_count.
     */
    public void recordBootMetrics(String tenantId, double durationSeconds, int moduleCount) {
        // Formatted structured output following OpenTelemetry semantic logging guidelines
        System.out.printf("[OTEL METRIC] name=dbos_boot_duration_seconds tenant=%s value=%.4fs status=SUCCESS%n", 
            tenantId, durationSeconds);
        System.out.printf("[OTEL METRIC] name=dbos_module_count tenant=%s value=%d%n", 
            tenantId, moduleCount);
    }

    /**
     * Captures trace spans out-of-band for deep operational execution path observability.
     */
    public void recordTraceSpan(String phase, String tenantId, long durationMs, Map<String, String> attributes) {
        StringBuilder spanBuilder = new StringBuilder();
        spanBuilder.append(String.format("[OTEL TRACE] phase=%s tenant=%s duration_ms=%d", phase, tenantId, durationMs));
        if (attributes != null && !attributes.isEmpty()) {
            attributes.forEach((k, v) -> spanBuilder.append(String.format(" attr.%s=%s", k, v)));
        }
        System.out.println(spanBuilder.toString());
    }
}

