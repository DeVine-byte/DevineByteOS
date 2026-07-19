package org.devinebyte.compiler.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsValidProject() throws IOException {

        Path src =
                Files.createDirectories(
                        tempDir.resolve("src")
                );

        Files.writeString(
                src.resolve("Main.dbos"),
                ""
        );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        tempDir,
                        src,
                        tempDir.resolve("build"),
                        false,
                        false
                );

        ProjectModel model =
                new ProjectLoader().load(configuration);

        assertEquals(
                1,
                model.sourceFileCount()
        );
    }

    @Test
    void findsBpFiles() throws IOException {

        Path src =
                Files.createDirectories(
                        tempDir.resolve("src")
                );

        Files.writeString(
                src.resolve("Service.bp"),
                ""
        );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        tempDir,
                        src,
                        tempDir.resolve("build"),
                        false,
                        false
                );

        ProjectModel model =
                new ProjectLoader().load(configuration);

        assertEquals(
                1,
                model.sourceFileCount()
        );
    }

    @Test
    void rejectsMissingProject() {

        Path missing =
                tempDir.resolve("missing");

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        missing,
                        missing.resolve("src"),
                        missing.resolve("build"),
                        false,
                        false
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectLoader().load(configuration)
        );
    }

    @Test
    void rejectsMissingSourceDirectory() {

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        tempDir,
                        tempDir.resolve("src"),
                        tempDir.resolve("build"),
                        false,
                        false
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectLoader().load(configuration)
        );
    }

    @Test
    void rejectsEmptyProject() throws IOException {

        Path src =
                Files.createDirectories(
                        tempDir.resolve("src")
                );

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        tempDir,
                        src,
                        tempDir.resolve("build"),
                        false,
                        false
                );

        assertThrows(
                IllegalStateException.class,
                () -> new ProjectLoader().load(configuration)
        );
    }
}
