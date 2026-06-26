package org.devinebyte.sdk.plugin;

public interface CompilerPlugin {

    String getId();

    String getName();

    String getVersion();

    void initialize(PluginContext context);

}
