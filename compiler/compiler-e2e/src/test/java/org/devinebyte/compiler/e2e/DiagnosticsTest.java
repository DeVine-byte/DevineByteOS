package org.devinebyte.compiler.e2e;

import org.devinebyte.compiler.testing.assertions.DiagnosticAssertions;
import org.junit.jupiter.api.Test;

class DiagnosticsTest extends CompilerE2ETestSupport {

    @Test
    void shouldProduceNoErrors() {
        CompilerResult result = compile("school");
        DiagnosticAssertions.hasNoErrors(result.diagnostics());
    }
}
