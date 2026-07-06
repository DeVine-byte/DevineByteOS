package org.devinebyte.compiler.api.internal;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.core.CompilerConfiguration;
import org.devinebyte.compiler.api.Session;

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
