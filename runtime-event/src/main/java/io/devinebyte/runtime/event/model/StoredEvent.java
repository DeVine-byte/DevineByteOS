package io.devinebyte.runtime.event.model;

import java.time.Instant;

public record StoredEvent(
    long sequence,
    DomainEvent event,
    String previousHash,
    String currentHash,
    Instant storedAt
) {}
