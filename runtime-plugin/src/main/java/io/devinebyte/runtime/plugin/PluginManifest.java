package io.devinebyte.runtime.plugin;

import java.util.List;
import java.util.Map;

public record PluginManifest(List<PluginEntry> plugins) {
    public record PluginEntry(
        String id,
        String version,
        String artifact,
        String entrypoint,
        String sdkVersion,
        String moduleId,
        String sha256
    ) {}
}
