package org.devinebyte.sdk.diagnostics;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import java.nio.file.Path;

public final class SourceLocation {

    private final Path file;
    private final int line;
    private final int column;

    public SourceLocation(Path file, int line, int column) {
        this.file = file;
        this.line = line;
        this.column = column;
    }

    public Path getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
