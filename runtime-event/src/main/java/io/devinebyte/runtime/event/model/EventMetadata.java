package io.devinebyte.runtime.event.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventMetadata(
    UUID correlationId,
    UUID causationId,
    String sourceModule,
    Instant storedAt,
    Map<String, String> tags // tenantId, version, moduleId
) {}
