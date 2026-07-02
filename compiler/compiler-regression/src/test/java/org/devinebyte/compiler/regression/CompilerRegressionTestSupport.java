package org.devinebyte.compiler.regression;

import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.CompilerRequest;
import org.devinebyte.sdk.CompilerResult;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Session;

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
