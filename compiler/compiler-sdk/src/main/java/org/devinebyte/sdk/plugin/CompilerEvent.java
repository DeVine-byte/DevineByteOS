package org.devinebyte.sdk.plugin;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a compiler lifecycle event dispatched to plugins.
 */
public final class CompilerEvent {

    private final EventType type;
    private final Instant timestamp;
    private final Map<String, Object> attributes;

    public CompilerEvent(
            EventType type,
            Instant timestamp,
            Map<String, Object> attributes) {

        this.type = Objects.requireNonNull(type, "type");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.attributes = Map.copyOf(
                attributes == null
                        ? Collections.emptyMap()
                        : attributes);
    }

    public EventType getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}
