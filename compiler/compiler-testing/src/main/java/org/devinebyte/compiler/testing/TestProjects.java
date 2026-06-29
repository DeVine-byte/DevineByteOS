package org.devinebyte.compiler.testing;

import java.nio.file.Path;

public final class TestProjects {

    private TestProjects() {
    }

    public static Path helloWorld() {

        return Path.of(
                "src/test/resources/fixtures/projects/hello-world"
        );

    }

}
