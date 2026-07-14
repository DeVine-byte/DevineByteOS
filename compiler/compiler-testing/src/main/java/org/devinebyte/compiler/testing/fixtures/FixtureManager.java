package org.devinebyte.compiler.testing.fixtures;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class FixtureManager {

    private FixtureManager() {
    }

    /**
     * Returns the root of the shared fixture projects.
     * Works for both exploded classpaths and JAR resources.
     */
    public static Path projects() {
        URL url = FixtureManager.class.getResource("/fixtures/projects");

        if (url == null) {
            throw new IllegalStateException("Missing /fixtures/projects on classpath");
        }

        try {
            var uri = url.toURI();

            if ("jar".equalsIgnoreCase(uri.getScheme())) {
                FileSystem fs;

                try {
                    fs = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException ex) {
                    fs = FileSystems.newFileSystem(uri, Map.of());
                }

                return fs.getPath("/fixtures/projects");
            }

            return Path.of(uri);

        } catch (Exception e) {
            throw new RuntimeException("Unable to locate fixture projects.", e);
        }
    }

    /**
     * Copies a fixture project into a temporary writable directory.
     */
    public static Path project(String name) {
        Path src = projects().resolve(name);

        if (!Files.exists(src)) {
            throw new IllegalArgumentException("Missing fixture: " + name);
        }

        try {
            Path tmp = Files.createTempDirectory("e2e-" + name + "-");
            copyRecursively(src, tmp);
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy fixture: " + name, e);
        }
    }

    /**
     * Creates a fresh output directory for compiler artifacts.
     */
    public static Path outputDirectory() {
        try {
            return Files.createTempDirectory("e2e-out-");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyRecursively(Path src, Path dest) throws IOException {
        try (var stream = Files.walk(src)) {
            for (Path p : (Iterable<Path>) stream::iterator) {
                Path relative = src.relativize(p);
                Path target = dest.resolve(relative.toString());

                if (Files.isDirectory(p)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(
                        p,
                        target,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        }
    }
}
