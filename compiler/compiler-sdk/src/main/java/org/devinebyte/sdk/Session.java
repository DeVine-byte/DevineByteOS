package org.devinebyte.sdk;

import java.nio.file.Path;
import java.util.Objects;

import org.devinebyte.sdk.internal.CompilerFacade;

/**
 * Represents a compiler session.
 *
 * <p>A session encapsulates compiler configuration and provides
 * the entry point for creating compilation requests and invoking
 * the compiler.</p>
 */
public final class Session {

    private final Path projectRoot;
    private final Path sourceDirectory;
    private final Path outputDirectory;

    private final boolean incremental;
    private final boolean optimize;

    private final CompilerContext context;

    Session(
            Path projectRoot,
            Path sourceDirectory,
            Path outputDirectory,
            boolean incremental,
            boolean optimize,
            CompilerContext context) {

        this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot");
        this.sourceDirectory = Objects.requireNonNull(sourceDirectory, "sourceDirectory");
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
        this.context = Objects.requireNonNull(context, "context");

        this.incremental = incremental;
        this.optimize = optimize;
    }

    /**
     * Returns the project root directory.
     */
    public Path getProjectRoot() {
        return projectRoot;
    }

    /**
     * Returns the source directory.
     */
    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Returns the compiler output directory.
     */
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns the compiler context associated with this session.
     */
    public CompilerContext getContext() {
        return context;
    }

    /**
     * Returns whether incremental compilation is enabled.
     */
    public boolean isIncremental() {
        return incremental;
    }

    /**
     * Returns whether compiler optimizations are enabled.
     */
    public boolean isOptimize() {
        return optimize;
    }

    /**
     * Creates a compilation request using this session's context.
     *
     * @param sourceFile source file to compile
     * @return compilation request
     */
    public Request request(Path sourceFile) {
        return new Request(
                sourceFile,
                outputDirectory,
                context,
                incremental
        );
    }

    /**
     * Compiles the supplied request.
     *
     * @param request compilation request
     * @return compilation result
     */
    public Result compile(Request request) {
        return new CompilerFacade().compile(this, request);
    }
}
