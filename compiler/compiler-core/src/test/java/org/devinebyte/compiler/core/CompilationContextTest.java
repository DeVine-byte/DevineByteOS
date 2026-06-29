package org.devinebyte.compiler.core;

import org.devinebyte.compiler.testing.support.CompilerTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompilationContextTest extends CompilerTestSupport {

    @Test
    void shouldCreateCompilationContext() {

        CompilationContext context = new CompilationContext();

        assertNotNull(context);

    }

}
