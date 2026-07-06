package org.devinebyte.compiler.performance;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.fixtures.FixtureManager;
import java.nio.file.Path;

public final class BenchmarkFixtures {

    private BenchmarkFixtures() {}

    public static Path project(String size) {
        return FixtureManager.project("benchmark-" + size);
    }

    public static Path smallProject() { return project("small"); }
    public static Path mediumProject() { return project("medium"); }
    public static Path largeProject() { return project("large"); }
    public static Path enterpriseProject() { return project("enterprise"); }
    public static Path multiModuleProject() { return project("multi-module"); }

    public static Path outputDirectory() {
        return FixtureManager.outputDirectory();
    }
}
