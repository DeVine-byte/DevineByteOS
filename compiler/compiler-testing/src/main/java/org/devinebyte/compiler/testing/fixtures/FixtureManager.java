package org.devinebyte.compiler.testing.fixtures;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

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
    public static Path projects() {
        URL url = FixtureManager.class.getResource("/fixtures/projects");
        if (url == null) {
            throw new IllegalStateException("Missing /fixtures/projects on classpath");
        }
        try {
            var uri = url.toURI();
            
            if ("jar".equals(uri.getScheme())) {
                try {
                    return java.nio.file.FileSystems
                        .getFileSystem(uri)
                        .getPath("/fixtures/projects");
                } catch (java.nio.file.FileSystemNotFoundException e) {
                    return java.nio.file.FileSystems
                        .newFileSystem(uri, java.util.Map.of())
                        .getPath("/fixtures/projects");
                }
            }
            
            return Path.of(uri);
        
        } catch (Exception e) {
            throw new RuntimeException(e);
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
