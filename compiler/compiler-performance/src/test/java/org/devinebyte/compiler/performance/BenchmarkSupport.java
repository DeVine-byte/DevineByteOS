package org.devinebyte.compiler.performance;

import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public abstract class BenchmarkSupport {

    /**
     * Benchmarks compilation using the default benchmark output directory.
     */
    protected BenchmarkResult benchmark(Path input) {
        return benchmark(input, BenchmarkFixtures.outputDirectory());
    }

    /**
     * Benchmarks incremental compilation using the default benchmark output directory.
     */
    protected BenchmarkResult benchmarkIncremental(Path input) {
        return benchmarkIncremental(input, BenchmarkFixtures.outputDirectory());
    }

    /**
     * Benchmarks a full compilation.
     */
    protected BenchmarkResult benchmark(
            Path input,
            Path output) {

        Session session = CompilerSDK.builder()
                .projectRoot(input)
                .sourceDirectory(input)
                .outputDirectory(output)
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
     * Benchmarks an incremental compilation.
     */
    protected BenchmarkResult benchmarkIncremental(
            Path input,
            Path output) {

        Session session = CompilerSDK.builder()
                .projectRoot(input)
                .sourceDirectory(input)
                .outputDirectory(output)
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
