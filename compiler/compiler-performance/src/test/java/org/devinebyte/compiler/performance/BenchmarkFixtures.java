package org.devinebyte.compiler.performance;

import org.devinebyte.compiler.testing.fixtures.FixtureManager;

import java.nio.file.Path;

/**
 * Provides benchmark fixture projects.
 *
 * All benchmark projects are backed by the same fixture
 * directory used by the compiler-e2e tests.
 */
public final class BenchmarkFixtures {

    private BenchmarkFixtures() {
    }

    public static Path helloWorldProject() {
        return FixtureManager.project("hello-world");
    }

    public static Path clinicProject() {
        return FixtureManager.project("clinic");
    }

    public static Path schoolProject() {
        return FixtureManager.project("school");
    }

    public static Path warehouseProject() {
        return FixtureManager.project("warehouse");
    }

    public static Path enterpriseProject() {
        return FixtureManager.project("enterprise");
    }

    public static Path crmProject() {
        return FixtureManager.project("crm");
    }
    public static Path multiModuleProject() {
        return FixtureManager.project("multi-module");
    }

    public static Path invalidProject() {
        return FixtureManager.project("invalid-project");
    }

    public static Path outputDirectory() {
        return FixtureManager.outputDirectory();
    }
}
