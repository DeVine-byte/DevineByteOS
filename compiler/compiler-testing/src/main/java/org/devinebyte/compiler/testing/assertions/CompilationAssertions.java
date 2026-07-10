package org.devinebyte.compiler.testing.assertions;

import org.devinebyte.sdk.Result;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions for compilation results.
 */
public final class CompilationAssertions {

    private CompilationAssertions() {
    }

    /**
     * Asserts that compilation completed successfully.
     *
     * @param result compilation result
     */
    public static void assertSuccessful(Result result) {
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
    }

    /**
     * Asserts that compilation failed.
     *
     * @param result compilation result
     */
    public static void assertFailed(Result result) {
        assertThat(result).isNotNull();
        assertThat(result.success()).isFalse();
    }

    /**
     * Asserts the number of generated artifacts.
     *
     * @param result compilation result
     * @param expected expected artifact count
     */
    public static void assertArtifactCount(Result result, int expected) {
        assertThat(result).isNotNull();
        assertThat(result.artifacts())
                .hasSize(expected);
    }

    /**
     * Asserts the number of diagnostics.
     *
     * @param result compilation result
     * @param expected expected diagnostic count
     */
    public static void assertDiagnosticCount(Result result, int expected) {
        assertThat(result).isNotNull();
        assertThat(result.diagnostics())
                .hasSize(expected);
    }
}
