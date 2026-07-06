package org.devinebyte.sdk;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import java.nio.file.Path;

public class Builder {

    private Path projectRoot;
    private Path sourceDirectory;
    private Path outputDirectory;
    private boolean incremental;
    private boolean optimize;

    public Builder projectRoot(Path root) {
        this.projectRoot = root;
        return this;
    }

    public Builder sourceDirectory(Path source) {
        this.sourceDirectory = source;
        return this;
    }

    public Builder outputDirectory(Path output) {
        this.outputDirectory = output;
        return this;
    }

    public Builder incremental(boolean value) {
        this.incremental = value;
        return this;
    }

    public Builder optimize(boolean value) {
        this.optimize = value;
        return this;
    }

    public Session build() {
        return new Session(
                projectRoot,
                sourceDirectory,
                outputDirectory,
                incremental,
                optimize
        );
    }

}
