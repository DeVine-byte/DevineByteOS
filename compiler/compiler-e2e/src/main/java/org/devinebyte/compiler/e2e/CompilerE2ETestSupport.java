package org.devinebyte.compiler.e2e;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.CompilerRequest;
import org.devinebyte.compiler.api.CompilerResult;
import org.devinebyte.compiler.api.CompilerSDK;
import org.devinebyte.compiler.api.Session;

import java.nio.file.Path;

public abstract class CompilerE2ETestSupport {

    protected CompilerResult compile(String projectName) {
        Path project = FixtureManager.project(projectName);
        Path output = FixtureManager.outputDirectory();

        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();
        CompilerContext ctx = sdk.createContext();

        CompilerRequest request = new CompilerRequest(project, output, ctx, false);
        return session.compile(request);
    }

    protected CompilerResult compileIncremental(String projectName) {
        Path project = FixtureManager.project(projectName);
        Path output = FixtureManager.outputDirectory();

        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();
        CompilerContext ctx = sdk.createContext();

        CompilerRequest first = new CompilerRequest(project, output, ctx, false);
        session.compile(first); // warm cache

        CompilerRequest second = new CompilerRequest(project, output, ctx, true);
        return session.compile(second);
    }
}
