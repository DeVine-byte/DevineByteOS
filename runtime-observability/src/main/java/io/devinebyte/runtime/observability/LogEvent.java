package io.devinebyte.runtime.observability;

import java.time.Instant;
import java.util.Map;

public record LogEvent(
    Instant timestamp,
    String level,
    String tenantId,
    String moduleId,
    String version,
    String code,
    String message,
    Map<String, Object> kv,
    String traceId,
    String spanId,
    String eventId,
    String workflowId,
    String correlationId,
    String errorType,
    Long durationMs
) {}
