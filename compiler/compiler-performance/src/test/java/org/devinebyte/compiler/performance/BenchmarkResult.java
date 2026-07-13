package org.devinebyte.compiler.performance;

import org.devinebyte.sdk.Result;

import java.time.Duration;

/**
 * Encapsulates the outcome of a benchmark execution.
 *
 * Stores the SDK compilation result together with the
 * elapsed execution time.
 */
public record BenchmarkResult(
        Result result,
        Duration duration
) {

    /**
     * Returns the benchmark duration in milliseconds.
     */
    public long milliseconds() {
        return duration.toMillis();
    }

    /**
     * Returns the benchmark duration in nanoseconds.
     */
    public long nanoseconds() {
        return duration.toNanos();
    }
}
