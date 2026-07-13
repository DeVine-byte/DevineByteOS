package org.devinebyte.compiler.performance;

import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Shared benchmark infrastructure.
 *
 * All compiler performance benchmarks should extend this class.
 */
public abstract class BenchmarkSupport {

    /**
     * Executes a normal compilation benchmark.
     */
    protected BenchmarkResult benchmark(Path projectDirectory) {

        Path outputDirectory = BenchmarkFixtures.outputDirectory();

        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();

        Request request = Request.builder()
                .projectDirectory(projectDirectory)
                .outputDirectory(outputDirectory)
                .incremental(false)
                .build();

        Instant start = Instant.now();

        Result result = session.compile(request);

        Instant finish = Instant.now();

        return new BenchmarkResult(
                result,
                Duration.between(start, finish)
        );
    }

    /**
     * Executes an incremental compilation benchmark.
     */
    protected BenchmarkResult benchmarkIncremental(Path projectDirectory) {

        Path outputDirectory = BenchmarkFixtures.outputDirectory();

        CompilerSDK sdk = CompilerSDK.builder().build();
        Session session = sdk.createSession();

        Request initial = Request.builder()
                .projectDirectory(projectDirectory)
                .outputDirectory(outputDirectory)
                .incremental(false)
                .build();

        session.compile(initial);

        Request incremental = Request.builder()
                .projectDirectory(projectDirectory)
                .outputDirectory(outputDirectory)
                .incremental(true)
                .build();

        Instant start = Instant.now();

        Result result = session.compile(incremental);

        Instant finish = Instant.now();

        return new BenchmarkResult(
                result,
                Duration.between(start, finish)
        );
    }
}
