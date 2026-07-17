package org.devinebyte.compiler.core;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a single source file loaded into memory.
 *
 * <p>This class is immutable and is passed through the
 * compiler pipeline until lexing begins.</p>
 */
public final class SourceFile {

    private final Path path;
    private final String content;

    public SourceFile(Path path, String content) {
        this.path = Objects.requireNonNull(path, "path");
        this.content = Objects.requireNonNull(content, "content");
    }

    /**
     * Returns the source file path.
     */
    public Path path() {
        return path;
    }

    /**
     * Returns the source text.
     */
    public String content() {
        return content;
    }

    /**
     * Returns the filename.
     */
    public String fileName() {
        return path.getFileName().toString();
    }

    /**
     * Returns the file extension.
     */
    public String extension() {
        String name = fileName();
        int index = name.lastIndexOf('.');
        return index >= 0 ? name.substring(index) : "";
    }

    /**
     * Returns the number of characters in the source.
     */
    public int length() {
        return content.length();
    }

    /**
     * Returns true if the source file is empty.
     */
    public boolean isEmpty() {
        return content.isBlank();
    }

    @Override
    public String toString() {
        return "SourceFile{" +
                "path=" + path +
                ", length=" + length() +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SourceFile other)) {
            return false;
        }

        return path.equals(other.path)
                && content.equals(other.content);
    }
}
