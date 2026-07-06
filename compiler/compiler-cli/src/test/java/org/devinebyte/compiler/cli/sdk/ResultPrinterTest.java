package org.devinebyte.compiler.cli.sdk; // Fixed: org not com
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.console.AnsiConsole; // Fixed: org not com
import org.devinebyte.compiler.api.CompilerResult; // Fixed: org.devinebyte.compiler.api.*
import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.Severity;
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
