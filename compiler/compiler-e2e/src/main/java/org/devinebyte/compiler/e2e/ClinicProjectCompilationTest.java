package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.CompilationAssertions;
import org.junit.jupiter.api.Test;

class ClinicProjectCompilationTest extends CompilerE2ETestSupport {

    @Test
    void shouldCompileClinicBlueprint() {
        CompilerResult result = compile("clinic");
        CompilationAssertions.succeeded(result.success());
    }
}
