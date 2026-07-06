package org.devinebyte.compiler.regression;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.CompilerRequest;
import org.devinebyte.compiler.api.CompilerResult;
import org.devinebyte.compiler.api.CompilerSDK;
import org.devinebyte.compiler.api.Session;

import java.nio.file.Path;

public abstract class CompilerRegressionTestSupport {

    protected CompilerResult compile(Path project, Path output) {
        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();
        CompilerContext ctx = sdk.createContext();

        CompilerRequest request = new CompilerRequest(project, output, ctx, false);
        return session.compile(request);
    }
}
