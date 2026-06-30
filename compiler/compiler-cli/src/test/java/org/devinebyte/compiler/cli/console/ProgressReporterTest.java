package org.devinebyte.compiler.cli.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProgressReporterTest {

    @Test
    void shouldReportProgress() {

        Console console = new AnsiConsole();

        ProgressReporter reporter =
                new ProgressReporter(console);

        assertDoesNotThrow(() -> {

            reporter.start("Compilation");
            reporter.step("Parsing");
            reporter.finish("Compilation");

        });

    }

}
