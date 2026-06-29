package org.devinebyte.compiler.testing.fixtures;

import java.nio.file.Path;

public final class FixtureManager {

    private FixtureManager() {
    }

    public static Path projects() {

        return Path.of(
                "src/test/resources/fixtures/projects");

    }

}
