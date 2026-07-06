package org.devinebyte.compiler.core;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.testing.support.CompilerTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCompilerContextTest extends CompilerTestSupport {

    @Test
    void shouldCreateCompilerContext(@TempDir Path tempDir) {
        var ctx = new DefaultCompilerContext(tempDir, Map.of(), new DiagnosticCollector());
        assertNotNull(ctx);
        assertEquals(tempDir, ctx.workingDirectory());
        assertNotNull(ctx.diagnostics());
    }
}
