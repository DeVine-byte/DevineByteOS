package org.devinebyte.compiler.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompilerEngineTest {

    @TempDir
    Path fixtureProject;

    @Test
    void engineCompilesProjectSuccessfully() throws IOException {

        Path src =
                Files.createDirectories(
                        fixtureProject.resolve("src")
                );

        Files.writeString(
                src.resolve("hello.dbos"),
                "service Hello {}"
        );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        fixtureProject,
                        "hello-world",
                        "1.0.0"
                );

        CompilerEngine engine =
                new CompilerEngine(configuration);

        CompilerPipelineResult result =
                engine.compile();

        assertTrue(result.success());

        assertEquals(1, result.sourceFiles());

        assertTrue(result.generatedFiles() >= 0);
    }

    @Test
    void engineFailsForMissingProject() {

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        Path.of("missing"),
                        "demo",
                        "1.0"
                );

        CompilerEngine engine =
                new CompilerEngine(configuration);

        CompilerPipelineResult result =
                engine.compile();

        assertFalse(result.success());

        assertNotNull(result.message());
    }

    @Test
    void engineRejectsEmptyProject() throws IOException {

        Files.createDirectories(
                fixtureProject.resolve("src")
        );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        fixtureProject,
                        "demo",
                        "1.0"
                );

        CompilerEngine engine =
                new CompilerEngine(configuration);

        CompilerPipelineResult result =
                engine.compile();

        assertFalse(result.success());

        assertNotNull(result.message());
    }
}
