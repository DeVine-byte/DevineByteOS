package org.devinebyte.compiler.regression;

import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import java.nio.file.Path;

public final class RegressionFixtures {

    private RegressionFixtures() {}

    /** Maps to src/test/resources/regressions/{id} via FixtureManager */
    public static Path project(String id) {
        return FixtureManager.project(id);
    }

    public static Path outputDirectory() {
        return FixtureManager.outputDirectory();
    }
}
