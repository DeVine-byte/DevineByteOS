package org.devinebyte.compiler.performance;

import org.devinebyte.compiler.testing.fixtures.BenchmarkFixtures;
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
        Path input = projectDirectory;

        Session session = CompilerSDK.builder()
                .projectRoot(input)
                .sourceDirectory(input)
                .outputDirectory(outputDirectory)
                .incremental(false)
                .optimize(false)
                .build();

        Request request = session.request(input);

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
     * Note: Session is configured as incremental. No warmup needed because
     * the SDK should handle cache internally. If you need a warmup run, 
     * do a separate .incremental(false) build first.
     */
    protected BenchmarkResult benchmarkIncremental(Path projectDirectory) {
        Path outputDirectory = BenchmarkFixtures.outputDirectory();
        Path input = projectDirectory;

        Session session = CompilerSDK.builder()
                .projectRoot(input)
                .sourceDirectory(input)
                .outputDirectory(outputDirectory)
                .incremental(true)
                .optimize(false)
                .build();

        Request request = session.request(input);

        Instant start = Instant.now();
        Result result = session.compile(request);
        Instant finish = Instant.now();

        return new BenchmarkResult(
                result,
                Duration.between(start, finish)
        );
    }
}
