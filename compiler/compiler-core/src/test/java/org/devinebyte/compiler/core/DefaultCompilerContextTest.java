package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCompilerContextTest {

    @Test
    void storesValues() {

        File workingDirectory = new File("project");

        Map<String, String> options =
                Map.of("optimize", "true");

        List<Diagnostic> diagnostics = List.of();

        DefaultCompilerContext context =
                new DefaultCompilerContext(
                        workingDirectory,
                        options,
                        diagnostics
                );

        assertEquals(workingDirectory, context.workingDirectory());
        assertEquals(options, context.options());
        assertEquals(diagnostics, context.diagnostics());
    }

    @Test
    void nullOptionsBecomeEmptyMap() {

        DefaultCompilerContext context =
                new DefaultCompilerContext(
                        new File("."),
                        null,
                        List.of()
                );

        assertTrue(context.options().isEmpty());
    }

    @Test
    void nullDiagnosticsBecomeEmptyList() {

        DefaultCompilerContext context =
                new DefaultCompilerContext(
                        new File("."),
                        Map.of(),
                        null
                );

        assertTrue(context.diagnostics().isEmpty());
    }

    @Test
    void optionsAreImmutable() {

        DefaultCompilerContext context =
                new DefaultCompilerContext(
                        new File("."),
                        Map.of("a", "b"),
                        List.of()
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> context.options().put("x", "y")
        );
    }

    @Test
    void diagnosticsAreImmutable() {

        DefaultCompilerContext context =
                new DefaultCompilerContext(
                        new File("."),
                        Map.of(),
                        List.of()
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> context.diagnostics().add(null)
        );
    }
}
