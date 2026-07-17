package org.devinebyte.compiler.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SourceLoaderTest {

    @TempDir
    Path tempProject;

    @Test
    void shouldLoadSingleSourceFile() throws IOException {

        Path src = Files.createDirectories(tempProject.resolve("src"));

        Path file = src.resolve("Hello.dbos");

        Files.writeString(file, "service Hello {}");

        ProjectModel project = new ProjectModel(
                tempProject,
                src,
                List.of(file),
                "hello-world",
                "1.0.0"
        );

        SourceLoader loader = new SourceLoader();

        SourceProject sourceProject = loader.load(project);

        assertEquals(1, sourceProject.sourceFileCount());

        SourceFile source = sourceProject.sourceFiles().get(0);

        assertEquals(file, source.path());

        assertEquals("service Hello {}", source.content());

        assertFalse(source.isEmpty());
    }

    @Test
    void shouldLoadMultipleSourceFiles() throws IOException {

        Path src = Files.createDirectories(tempProject.resolve("src"));

        Path a = src.resolve("A.dbos");
        Path b = src.resolve("B.bp");

        Files.writeString(a, "entity A {}");
        Files.writeString(b, "workflow B {}");

        ProjectModel project = new ProjectModel(
                tempProject,
                src,
                List.of(a, b),
                "enterprise",
                "1.0.0"
        );

        SourceProject sources =
                new SourceLoader().load(project);

        assertEquals(2, sources.sourceFileCount());
    }

    @Test
    void shouldPreserveProjectModel() throws IOException {

        Path src = Files.createDirectories(tempProject.resolve("src"));

        Path file = src.resolve("Main.dbos");

        Files.writeString(file, "service Main {}");

        ProjectModel model = new ProjectModel(
                tempProject,
                src,
                List.of(file),
                "test-project",
                "2.0.0"
        );

        SourceProject sources =
                new SourceLoader().load(model);

        assertSame(model, sources.project());
    }

    @Test
    void shouldThrowWhenSourceFileDoesNotExist() {

        Path src = tempProject.resolve("src");

        Path missing = src.resolve("Missing.dbos");

        ProjectModel project = new ProjectModel(
                tempProject,
                src,
                List.of(missing),
                "missing",
                "1.0.0"
        );

        SourceLoader loader = new SourceLoader();

        assertThrows(
                IllegalStateException.class,
                () -> loader.load(project)
        );
    }

    @Test
    void shouldLoadEmptySourceFile() throws IOException {

        Path src = Files.createDirectories(tempProject.resolve("src"));

        Path file = src.resolve("Empty.dbos");

        Files.writeString(file, "");

        ProjectModel project = new ProjectModel(
                tempProject,
                src,
                List.of(file),
                "empty-project",
                "1.0.0"
        );

        SourceProject sources =
                new SourceLoader().load(project);

        SourceFile source =
                sources.sourceFiles().get(0);

        assertTrue(source.isEmpty());

        assertEquals(0, source.length());
    }

    @Test
    void shouldLoadSourceFileWithUnicodeCharacters() throws IOException {

        Path src = Files.createDirectories(tempProject.resolve("src"));

        Path file = src.resolve("Unicode.dbos");

        String content = """
                entity Patient {
                    name = "José"
                    city = "Lagos"
                }
                """;

        Files.writeString(file, content);

        ProjectModel project = new ProjectModel(
                tempProject,
                src,
                List.of(file),
                "unicode",
                "1.0.0"
        );

        SourceProject sources =
                new SourceLoader().load(project);

        assertEquals(content,
                sources.sourceFiles().get(0).content());
    }
    }
