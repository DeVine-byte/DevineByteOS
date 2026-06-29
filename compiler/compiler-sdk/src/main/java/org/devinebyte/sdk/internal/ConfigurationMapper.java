package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilerConfiguration;
import org.devinebyte.sdk.Session;

public final class ConfigurationMapper {

    private ConfigurationMapper() {
    }

    public static CompilerConfiguration map(Session session) {

        return new CompilerConfiguration(

                session.getOutputDirectory().toString(),

                false,

                session.isIncremental()

        );

    }

}
