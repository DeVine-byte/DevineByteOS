package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompilerEngineTest {

    private CompilerEngine newEngine() {
        CompilerConfiguration configuration =
                new CompilerConfiguration(
                        Path.of("project"),
                        Path.of("project/src"),
                        Path.of("project/build"),
                        false,
                        false
                );

        return new CompilerEngine(configuration);
    }

    @Test
    void compileReturnsResult() {

        CompilerEngine engine = newEngine();

        CompilationResult result = engine.compile();

        assertNotNull(result);
    }

    @Test
    void compileReturnsSuccess() {

        CompilerEngine engine = newEngine();

        CompilationResult result = engine.compile();

        assertTrue(result.success());
    }

    @Test
    void compileReturnsExpectedOutput() {

        CompilerEngine engine = newEngine();

        CompilationResult result = engine.compile();

        assertEquals(
            "Project loaded.",
            result.output()
        );

        assertNull(result.error());
    }
}
