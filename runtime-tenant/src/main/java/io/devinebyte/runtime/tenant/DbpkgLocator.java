package io.devinebyte.runtime.tenant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public final class DbpkgLocator {

    private static final Path DEFAULT_EXECUTION_DIR = Paths.get("execution");
    private static final Path ALTERNATE_BUILD_DIR = Paths.get("build/data/packages");

    /**
     * Resolves a .dbpkg file out-of-band for a given tenant using deterministic lookups.
     */
    public static Path locate(String tenantId) {
        // Priority 1: Check standard execution space directory context (execution/{tenantId}/ *.dbpkg)
        Path executionPath = DEFAULT_EXECUTION_DIR.resolve(tenantId);
        Optional<Path> found = findPackageInDirectory(executionPath);
        if (found.isPresent()) return found.get();

        // Priority 2: Fallback to active build compilation target outputs (build/data/packages/{tenantId}/ *.dbpkg)
        Path buildPath = ALTERNATE_BUILD_DIR.resolve(tenantId);
        found = findPackageInDirectory(buildPath);
        if (found.isPresent()) return found.get();

        // Priority 3: Final root level execution sweep search (execution/ *.dbpkg)
        found = findPackageInDirectory(DEFAULT_EXECUTION_DIR);
        if (found.isPresent()) return found.get();

        throw new IllegalArgumentException(
            "Out-of-Band Resolution Error: No package artifact (.dbpkg) discovered for tenant configuration: " + tenantId
        );
    }

    private static Optional<Path> findPackageInDirectory(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return Optional.empty();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                .filter(p -> p.toString().endsWith(".dbpkg"))
                .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}

