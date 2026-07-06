package org.devinebyte.compiler.cli.sdk; // Fixed: org not com
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.console.AnsiConsole; // Fixed: org not com
import org.devinebyte.sdk.CompilerResult; // Fixed: org.devinebyte.sdk.*
import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.Severity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ResultPrinterTest {

    @Test
    void shouldPrintCompilationResult() {

        ResultPrinter printer = new ResultPrinter(new AnsiConsole());

        // CompilerResult.success() is gone. Use record ctor. Audit §4
        CompilerResult result = new CompilerResult(
            List.of(Path.of("out/User.java")), // artifacts
            List.of(new Diagnostic(DiagnosticSeverity.INFO, "INF001", "Compilation succeeded", null))
        );

        assertDoesNotThrow(() -> printer.print(result));
    }
}
