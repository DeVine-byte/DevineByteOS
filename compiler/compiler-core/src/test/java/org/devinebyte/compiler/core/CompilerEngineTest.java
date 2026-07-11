package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompilerEngineTest {

    @Test
    void compileReturnsResult() {

        CompilerEngine engine = new CompilerEngine();

        CompilationResult result = engine.compile();

        assertNotNull(result);
    }

    @Test
    void compileReturnsSuccess() {

        CompilerEngine engine = new CompilerEngine();

        CompilationResult result = engine.compile();

        assertTrue(result.success());
    }

    @Test
    void compileReturnsExpectedOutput() {

        CompilerEngine engine = new CompilerEngine();

        CompilationResult result = engine.compile();

        assertEquals(
                "Compilation completed.",
                result.output()
        );

        assertNull(result.error());
    }
}
