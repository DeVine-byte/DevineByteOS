package org.devinebyte.compiler.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompilerConfigurationTest {

    @Test
    void shouldStoreConfigurationValues() {

        Path project = Path.of("project");
        Path source = project.resolve("src");
        Path output = project.resolve("build");

        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        project,
                        source,
                        output,
                        true,
                        false
                );

        assertEquals(project, configuration.projectRoot());
        assertEquals(source, configuration.sourceDirectory());
        assertEquals(output, configuration.outputDirectory());

        assertTrue(configuration.incremental());
        assertFalse(configuration.optimize());
    }
}
