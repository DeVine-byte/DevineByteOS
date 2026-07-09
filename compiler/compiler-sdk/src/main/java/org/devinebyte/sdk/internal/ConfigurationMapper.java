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
                session.getOutputDirectory().toString(),
                false,                      // strictMode (future SDK option)
                session.isIncremental()
        );
    }
}
