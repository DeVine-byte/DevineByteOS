package org.devinebyte.compiler.core;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CompilerLifecycleTest {

    @Test
    void shouldAllowMultipleStarts() {

        CompilerEngine engine = new CompilerEngine();

        assertDoesNotThrow(engine::start);

        assertDoesNotThrow(engine::start);

    }

}
