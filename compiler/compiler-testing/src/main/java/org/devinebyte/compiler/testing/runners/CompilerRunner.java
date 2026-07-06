package org.devinebyte.compiler.testing.runners;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.sdk.CompilerSDK;
import org.devinebyte.compiler.sdk.Result;
import org.devinebyte.compiler.sdk.Session;
import org.devinebyte.compiler.sdk.Request;

public final class CompilerRunner {

    public Result run(Session session,
                      Request request) {

        return session.compile(request);

    }

}
