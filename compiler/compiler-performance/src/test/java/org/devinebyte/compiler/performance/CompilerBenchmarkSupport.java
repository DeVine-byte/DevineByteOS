package org.devinebyte.compiler.performance;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.CompilerRequest;
import org.devinebyte.sdk.CompilerResult;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Session;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public abstract class CompilerBenchmarkSupport {

    protected BenchmarkResult benchmark(Path input, Path output) {
        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();
        CompilerContext ctx = sdk.createContext();

        CompilerRequest request = new CompilerRequest(input, output, ctx, false);

        Instant start = Instant.now();
        CompilerResult result = session.compile(request);
        Instant finish = Instant.now();

        return new BenchmarkResult(result, Duration.between(start, finish));
    }

    protected BenchmarkResult benchmarkIncremental(Path input, Path output) {
        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();
        CompilerContext ctx = sdk.createContext();

        session.compile(new CompilerRequest(input, output, ctx, false)); // warm

        Instant start = Instant.now();
        CompilerResult result = session.compile(new CompilerRequest(input, output, ctx, true));
        Instant finish = Instant.now();

        return new BenchmarkResult(result, Duration.between(start, finish));
    }
}
