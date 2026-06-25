package org.devinebyte.sdk;

import java.nio.file.Path;

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
            boolean optimize
    ) {
        this.projectRoot = projectRoot;
        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
        this.incremental = incremental;
        this.optimize = optimize;
    }

    public Result compile(Request request) {

        // implemented later

        return Result.success();
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
}
