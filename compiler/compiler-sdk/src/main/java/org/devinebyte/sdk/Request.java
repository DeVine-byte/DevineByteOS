package org.devinebyte.sdk;

import java.nio.file.Path;
import java.util.Objects;

public final class Request {

    private final Path sourceFile;
    private final Path outputDirectory;
    private final CompilerContext context;
    private final boolean incremental;

    public Request(
            Path sourceFile,
            Path outputDirectory,
            CompilerContext context,
            boolean incremental) {

        this.sourceFile = Objects.requireNonNull(sourceFile, "sourceFile");
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
        this.context = Objects.requireNonNull(context, "context");
        this.incremental = incremental;
    }

    public Path getSourceFile() {
        return sourceFile;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public CompilerContext getContext() {
        return context;
    }

    public boolean isIncremental() {
        return incremental;
    }
}
