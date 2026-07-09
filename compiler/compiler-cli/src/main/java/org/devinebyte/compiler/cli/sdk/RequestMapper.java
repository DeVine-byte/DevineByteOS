package org.devinebyte.compiler.cli.sdk;

import java.io.File;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Session;

public final class RequestMapper {

    /**
     * Converts CLI options into an SDK Request.
     */
    public Request compileRequest(Session session, CliOptions options) {

        File sourceFile = options.getInput().toFile();
        File outputDirectory = options.getOutput().toFile();

        CompilerContext context = session.getContext();

        return new Request(
                sourceFile,
                outputDirectory,
                context,
                session.isIncremental()
        );
    }
}
