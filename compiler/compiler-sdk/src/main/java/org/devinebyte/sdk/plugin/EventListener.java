package org.devinebyte.sdk.plugin;

/**
 * Receives compiler lifecycle events.
 */
@FunctionalInterface
public interface EventListener {

    /**
     * Invoked whenever a compiler event is published.
     *
     * @param event the compiler event
     */
    void onEvent(CompilerEvent event);
}
