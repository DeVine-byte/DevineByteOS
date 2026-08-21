package io.devinebyte.runtime.event.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.runtime.core.context.TenantContext;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
    String type,
    String version,
    ObjectNode payload,
    EventMetadata metadata
) {
    // NEW: 5-arg version with explicit sourceModule
    public static DomainEvent create(TenantContext ctx, String sourceModule, String type, String version, ObjectNode payload) {
        EventMetadata meta = new EventMetadata(
            UUID.randomUUID(),
            null,
            sourceModule,
            Instant.now(),
            Map.of("tenantId", ctx.tenantId())
        );
        return new DomainEvent(type, version, payload, meta);
    }

    // BACKWARDS COMPAT: 4-arg version defaults to "runtime"
    public static DomainEvent create(TenantContext ctx, String type, String version, ObjectNode payload) {
        return create(ctx, "runtime", type, version, payload);
    }

    public Instant occurredAt() { return metadata.storedAt(); }
}

