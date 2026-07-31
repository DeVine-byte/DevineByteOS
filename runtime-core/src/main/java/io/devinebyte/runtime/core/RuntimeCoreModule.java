package io.devinebyte.runtime.core;

import jakarta.inject.Singleton;

/**
 * Marker for Jakarta DI. All runtime-core singletons are bound here.
 */
@Singleton
public class RuntimeCoreModule {
    // This module exists to be picked up by DI
}
