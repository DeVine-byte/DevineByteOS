package org.devinebyte.compiler.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectModelTest {

    @Test
    void shouldInstantiateProjectModel() {

        ProjectModel model =
                new ProjectModel(
                        Path.of("project"),
                        Path.of("project/src"),
                        List.of(
                                Path.of("project/src/Main.dbos")
                        ),
                        "demo",
                        "1.0.0"
                );

        assertNotNull(model);

        assertEquals(
                Path.of("project"),
                model.projectRoot()
        );

        assertEquals(
                Path.of("project/src"),
                model.sourceDirectory()
        );

        assertEquals(
                1,
                model.sourceFileCount()
        );

        assertEquals(
                "demo",
                model.projectName()
        );

        assertEquals(
                "1.0.0",
                model.version()
        );
    }
}
