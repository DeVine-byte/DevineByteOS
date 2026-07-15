package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;
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
    void engineInvokesLoaderSuccessfully() throws IOException {

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

        assertTrue(
                result.output().contains("Project loaded")
        );
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

        assertNotNull(result.error());
    }

}
