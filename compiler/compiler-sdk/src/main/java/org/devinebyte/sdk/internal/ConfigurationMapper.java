package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilerConfiguration;
import org.devinebyte.sdk.Session;

/**
 * Maps SDK Session -> CompilerConfiguration.
 */
public final class ConfigurationMapper {

    private ConfigurationMapper() {
    }

    public static CompilerConfiguration map(Session session) {

        return new CompilerConfiguration(
                session.getProjectRoot(),
                session.getSourceDirectory(),
                session.getOutputDirectory(),
                session.isIncremental(),
                session.isOptimize()
        );
    }
}
