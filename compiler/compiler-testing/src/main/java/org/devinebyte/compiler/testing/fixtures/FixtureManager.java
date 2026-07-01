package org.devinebyte.compiler.testing.fixtures;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FixtureManager {

    private FixtureManager() {}

    /** Base fixtures dir from classpath */
    public static Path projects() {
        URL url = FixtureManager.class.getResource("/fixtures/projects");
        if (url == null) {
            throw new IllegalStateException("Missing /fixtures/projects on classpath");
        }
        try { 
            return Path.of(url.toURI()); 
        } catch (URISyntaxException e) { 
            throw new RuntimeException(e); 
        }
    }

    /** Copy fixture to temp dir so tests can write to it */
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

    /** Fresh temp output dir per test */
    public static Path outputDirectory() {
        try { 
            return Files.createTempDirectory("e2e-out-"); 
        } catch (IOException e) { 
            throw new RuntimeException(e); 
        }
    }

    private static void copyRecursively(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(p -> {
            try {
                Path target = dest.resolve(src.relativize(p));
                Files.createDirectories(target.getParent());
                if (!Files.isDirectory(p)) {
                    Files.copy(p, target);
                }
            } catch (IOException e) { 
                throw new RuntimeException(e); 
            }
        });
    }
}
