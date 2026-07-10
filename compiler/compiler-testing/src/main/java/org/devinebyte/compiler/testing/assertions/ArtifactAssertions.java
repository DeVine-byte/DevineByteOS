package org.devinebyte.compiler.testing.assertions;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions for generated compiler artifacts.
 */
public final class ArtifactAssertions {

    private ArtifactAssertions() {
    }

    /**
     * Asserts that an artifact exists.
     *
     * @param artifact artifact path
     */
    public static void assertExists(Path artifact) {
        assertThat(artifact)
                .isNotNull();

        assertThat(Files.exists(artifact))
                .isTrue();
    }

    /**
     * Asserts that an artifact does not exist.
     *
     * @param artifact artifact path
     */
    public static void assertDoesNotExist(Path artifact) {
        assertThat(artifact)
                .isNotNull();

        assertThat(Files.exists(artifact))
                .isFalse();
    }

    /**
     * Asserts that an artifact is a regular file.
     *
     * @param artifact artifact path
     */
    public static void assertIsFile(Path artifact) {
        assertExists(artifact);

        assertThat(Files.isRegularFile(artifact))
                .isTrue();
    }

    /**
     * Asserts that an artifact is a directory.
     *
     * @param artifact artifact path
     */
    public static void assertIsDirectory(Path artifact) {
        assertExists(artifact);

        assertThat(Files.isDirectory(artifact))
                .isTrue();
    }

    /**
     * Asserts that an artifact is not empty.
     *
     * @param artifact artifact path
     */
    public static void assertNotEmpty(Path artifact) throws Exception {
        assertExists(artifact);

        assertThat(Files.size(artifact))
                .isGreaterThan(0L);
    }
}
