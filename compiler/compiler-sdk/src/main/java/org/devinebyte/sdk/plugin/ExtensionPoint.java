package org.devinebyte.sdk.plugin;

import java.util.Objects;

/**
 * Represents a named compiler extension point.
 *
 * Extension points identify locations in the compiler where plugins
 * may contribute functionality.
 */
public final class ExtensionPoint {

    private final String name;
    private final String description;

    public ExtensionPoint(String name, String description) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description == null ? "" : description;
    }

    /**
     * Unique extension point identifier.
     */
    public String getName() {
        return name;
    }

    /**
     * Human-readable description.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExtensionPoint other)) return false;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
