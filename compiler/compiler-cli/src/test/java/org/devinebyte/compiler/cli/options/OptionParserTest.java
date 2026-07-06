package org.devinebyte.compiler.cli.options;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OptionParserTest {

    @Test
    void shouldParseInputOutputAndFlags() {

        OptionParser parser = new OptionParser();

        CliOptions options = parser.parse(new String[]{
                "--input", "src",
                "--output", "build",
                "--incremental",
                "--strict",
                "--optimize",
                "--verbose"
        });

        assertEquals(Path.of("src"), options.getInput());
        assertEquals(Path.of("build"), options.getOutput());
        assertTrue(options.isIncremental());
        assertTrue(options.isStrict());
        assertTrue(options.isOptimize());
        assertTrue(options.isVerbose());

    }

    @Test
    void shouldThrowExceptionForUnknownOption() {

        OptionParser parser = new OptionParser();

        assertThrows(
                OptionException.class,
                () -> parser.parse(new String[]{"--unknown"})
        );

    }

}
