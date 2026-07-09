package org.devinebyte.sdk.plugin;

/**
 * Base contract for all DevineByte compiler plugins.
 *
 * Plugins extend compiler functionality through stable extension
 * points without depending on compiler internals.
 */
public interface CompilerPlugin {

    /**
     * Globally unique plugin identifier.
     */
    String getId();

    /**
     * Human-readable plugin name.
     */
    String getName();

    /**
     * Plugin version.
     */
    String getVersion();

    /**
     * Called once when the plugin is loaded.
     */
    void initialize(PluginContext context);

    /**
     * Called before the plugin is unloaded.
     */
    default void shutdown() {
        // Default no-op
    }

    /**
     * Returns the extension point this plugin contributes to.
     */
    ExtensionPoint extensionPoint();

    /**
     * Returns the plugin's event listener.
     *
     * Plugins may return null if they do not subscribe to compiler
     * lifecycle events.
     */
    default EventListener eventListener() {
        return null;
    }
}
