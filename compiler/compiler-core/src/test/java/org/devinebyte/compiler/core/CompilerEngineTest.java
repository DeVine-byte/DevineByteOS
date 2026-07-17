package org.devinebyte.compiler.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.devinebyte.compiler.api.CompilationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

        CompilationResult result =
                engine.compile();

        assertTrue(result.success());

        assertNotNull(result.output());

        assertTrue(
                result.output().startsWith(
                        "Compilation completed successfully."
                )
        );

        assertNull(result.error());
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

        CompilationResult result =
                engine.compile();

        assertFalse(result.success());

        assertNull(result.output());

        assertNotNull(result.error());
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

        CompilationResult result =
                engine.compile();

        assertFalse(result.success());

        assertNotNull(result.error());
    }
}
