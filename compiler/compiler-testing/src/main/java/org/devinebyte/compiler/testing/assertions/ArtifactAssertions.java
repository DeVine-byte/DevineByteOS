package org.devinebyte.compiler.testing.assertions;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public final class ArtifactAssertions {

    private ArtifactAssertions() {
    }

    public static void exists(Path artifact) {

        assertThat(Files.exists(artifact))
                .isTrue();

    }

}
