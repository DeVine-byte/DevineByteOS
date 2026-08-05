package io.devinebyte.runtime.projection.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record ProjectionResult(
    String projectionName,
    JsonNode output,
    Instant computedAt
) {}
