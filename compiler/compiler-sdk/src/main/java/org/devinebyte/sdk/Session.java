package org.devinebyte.sdk;

import java.nio.file.Path;
import java.util.Objects;

public final class Session {

    private final Path projectRoot;
    private final Path sourceDirectory;
    private final Path outputDirectory;
    private final boolean incremental;
    private final boolean optimize;

    Session(
            Path projectRoot,
            Path sourceDirectory,
            Path outputDirectory,
            boolean incremental,
            boolean optimize) {

        this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot");
        this.sourceDirectory = Objects.requireNonNull(sourceDirectory, "sourceDirectory");
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");

        this.incremental = incremental;
        this.optimize = optimize;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public boolean isOptimize() {
        return optimize;
    }

    /**
     * Creates a compilation request for a source file.
     */
    public Request request(Path sourceFile, CompilerContext context) {
        return new Request(
                sourceFile,
                outputDirectory,
                context,
                incremental
        );
    }

    /**
     * Compiles a request using the SDK facade.
     */
    public Result compile(Request request) {
        return new org.devinebyte.sdk.internal.CompilerFacade()
                .compile(this, request);
    }
}
