package org.devinebyte.compiler.core;

import org.devinebyte.compiler.testing.support.CompilerTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompilerEngineTest extends CompilerTestSupport {

    @Test
    void shouldCreateCompilerEngine() {

        CompilerEngine engine = new CompilerEngine();

        assertNotNull(engine);

    }

    @Test
    void shouldStartWithoutThrowingException() {

        CompilerEngine engine = new CompilerEngine();

        assertDoesNotThrow(engine::start);

    }

}
