package io.devinebyte.runtime.config;

import java.time.Instant;

public record ManifestDTO(
    String schemaVersion,
    String tenantId,
    String version,
    Instant builtAt,
    String builtBy,
    String checksumSha256,
    String signature
) {}
