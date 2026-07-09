package org.devinebyte.compiler.cli.sdk;

import java.io.File;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Session;

public final class RequestMapper {

    public Request compileRequest(Session session, CliOptions options) {

        File sourceFile = options.getSourceFile().toFile();
        File outputDirectory = session.getOutputDirectory().toFile();

        return new Request(
                sourceFile,
                outputDirectory,
                session,
                options.isIncremental()
        );
    }
}
