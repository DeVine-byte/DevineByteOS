package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompilerRequestTest {

    @Test
    void shouldBuildRequest() {

        CompilerRequest request =
                CompilerRequest.builder()
                        .input(Path.of("src"))
                        .output(Path.of("build"))
                        .incremental(true)
                        .strict(false)
                        .verbose(true)
                        .build();

        assertEquals(Path.of("src"), request.input());

        assertEquals(Path.of("build"), request.output());

        assertTrue(request.incremental());

        assertFalse(request.strict());

        assertTrue(request.verbose());

    }

}
