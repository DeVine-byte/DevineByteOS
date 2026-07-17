package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilerConfiguration;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Session;

public final class ConfigurationMapper {

    private ConfigurationMapper() {
    }

    public static CompilerConfiguration map(
            Session session,
            Request request
    ) {

        return new CompilerConfiguration(

                session.getProjectRoot(),

                session.getContext(),

                request.getOutputDirectory(),

                session.isIncremental(),

                session.isOptimize()

        );
    }
}
