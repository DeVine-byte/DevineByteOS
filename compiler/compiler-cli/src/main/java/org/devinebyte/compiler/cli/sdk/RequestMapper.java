package org.devinebyte.compiler.cli.sdk;

import java.nio.file.Path;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Session;

public final class RequestMapper {

    public Request compileRequest(Session session, CliOptions options) {

        Path sourceFile = options.getInput();

        CompilerContext context = session.getContext();

        return new Request(
                sourceFile,
                session.getOutputDirectory(),
                context,
                session.isIncremental()
        );
    }
}
