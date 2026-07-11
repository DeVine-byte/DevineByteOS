package org.devinebyte.compiler.cli.options;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OptionParserTest {

    private final OptionParser parser = new OptionParser();

    @Test
    void parsesInputOption() {

        CliOptions options = parser.parse(new String[]{
                "--input", "project.db"
        });

        assertEquals(Path.of("project.db"), options.getInput());
    }

    @Test
    void parsesOutputOption() {

        CliOptions options = parser.parse(new String[]{
                "--output", "build"
        });

        assertEquals(Path.of("build"), options.getOutput());
    }

    @Test
    void parsesAllFlags() {

        CliOptions options = parser.parse(new String[]{
                "--input", "project.db",
                "--output", "build",
                "--incremental",
                "--optimize",
                "--strict",
                "--verbose"
        });

        assertEquals(Path.of("project.db"), options.getInput());
        assertEquals(Path.of("build"), options.getOutput());

        assertTrue(options.isIncremental());
        assertTrue(options.isOptimize());
        assertTrue(options.isStrict());
        assertTrue(options.isVerbose());
    }

    @Test
    void supportsShortOptions() {

        CliOptions options = parser.parse(new String[]{
                "-i", "input.db",
                "-o", "out",
                "-v"
        });

        assertEquals(Path.of("input.db"), options.getInput());
        assertEquals(Path.of("out"), options.getOutput());
        assertTrue(options.isVerbose());
    }

    @Test
    void throwsForUnknownOption() {

        OptionException exception = assertThrows(
                OptionException.class,
                () -> parser.parse(new String[]{"--unknown"})
        );

        assertTrue(exception.getMessage().contains("Unknown option"));
    }

    @Test
    void throwsWhenInputValueMissing() {

        OptionException exception = assertThrows(
                OptionException.class,
                () -> parser.parse(new String[]{"--input"})
        );

        assertTrue(exception.getMessage().contains("Missing value"));
    }

    @Test
    void throwsWhenOutputValueMissing() {

        OptionException exception = assertThrows(
                OptionException.class,
                () -> parser.parse(new String[]{"--output"})
        );

        assertTrue(exception.getMessage().contains("Missing value"));
    }
}
