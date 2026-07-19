package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CompilerEngineTest {

    @TempDir
    Path fixtureProject;

    private CompilerContext context(Path outputDirectory) {

        return new CompilerContext() {

            private final ArrayList<
                    org.devinebyte.compiler.api.diagnostics.Diagnostic> diagnostics =
                    new ArrayList<>();

            @Override
            public java.io.File workingDirectory() {
                return outputDirectory.toFile();
            }

            @Override
            public java.util.Map<String, String> options() {
                return new HashMap<>();
            }

            @Override
            public java.util.List<
                    org.devinebyte.compiler.api.diagnostics.Diagnostic> diagnostics() {
                return diagnostics;
            }
        };
    }

    @Test
    void engineCompilesProjectSuccessfully() throws IOException {

        Path src =
                Files.createDirectories(
                        fixtureProject.resolve("src")
                );

        Files.writeString(
                src.resolve("hello.dbos"),
                """
                entity Hello {
                }
                """
        );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        fixtureProject,
                        src,
                        fixtureProject.resolve("build"),
                        false,
                        false
                );

        CompilerEngine engine =
                new CompilerEngine(
                        configuration,
                        context(fixtureProject.resolve("build"))
                );

        CompilerPipelineResult result =
                engine.compile();

        assertTrue(result.success());
        assertEquals(1, result.sourceFiles());
        assertTrue(result.generatedFiles() >= 0);
    }

    @Test
    void engineFailsForMissingProject() {

        Path root = Path.of("missing");

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        root,
                        root.resolve("src"),
                        root.resolve("build"),
                        false,
                        false
                );

        CompilerEngine engine =
                new CompilerEngine(
                        configuration,
                        context(root.resolve("build"))
                );

        CompilerPipelineResult result =
                engine.compile();

        assertFalse(result.success());
        assertNotNull(result.message());
    }

    @Test
    void engineRejectsEmptyProject() throws IOException {

        Path src =
                Files.createDirectories(
                        fixtureProject.resolve("src")
                );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        fixtureProject,
                        src,
                        fixtureProject.resolve("build"),
                        false,
                        false
                );

        CompilerEngine engine =
                new CompilerEngine(
                        configuration,
                        context(fixtureProject.resolve("build"))
                );

        CompilerPipelineResult result =
                engine.compile();

        assertFalse(result.success());
        assertNotNull(result.message());
    }
}
