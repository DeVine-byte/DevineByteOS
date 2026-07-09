package org.devinebyte.sdk.diagnostics;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Identifies a location within a source file.
 */
public final class SourceLocation {

    private final Path file;
    private final int line;
    private final int column;

    /**
     * Creates a source location.
     *
     * @param file   source file
     * @param line   one-based line number
     * @param column one-based column number
     */
    public SourceLocation(Path file, int line, int column) {
        this.file = Objects.requireNonNull(file, "file");
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the source file.
     */
    public Path getFile() {
        return file;
    }

    /**
     * Returns the one-based line number.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the one-based column number.
     */
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return file + ":" + line + ":" + column;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SourceLocation other)) {
            return false;
        }

        return line == other.line
                && column == other.column
                && file.equals(other.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, line, column);
    }
    }
