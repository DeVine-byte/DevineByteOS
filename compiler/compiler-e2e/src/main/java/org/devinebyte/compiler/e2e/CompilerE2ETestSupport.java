package org.devinebyte.compiler.e2e;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.CompilerRequest;
import org.devinebyte.sdk.CompilerResult;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Session;

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
