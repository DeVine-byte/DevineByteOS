package io.devinebyte.runtime.plugin;

public record PluginDescriptor(
    String id,
    String version,
    String entrypoint,
    String sdkVersion,
    String moduleId
) {}

