package io.devinebyte.runtime.event.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.runtime.core.context.TenantContext;
import java.time.Instant;
import java.util.UUID;

public record DomainEvent(
    String type,
    String version,
    ObjectNode payload,
    EventMetadata metadata
) {
    public static DomainEvent create(TenantContext ctx, String type, String version, ObjectNode payload) {
        EventMetadata meta = new EventMetadata(
            UUID.randomUUID(),
            null,
            "runtime-main",
            Instant.now(),
            java.util.Map.of("tenantId", ctx.tenantId())
        );
        return new DomainEvent(type, version, payload, meta);
    }

    public Instant occurredAt() { return metadata.storedAt(); }
}
