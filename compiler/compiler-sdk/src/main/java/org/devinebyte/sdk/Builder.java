package org.devinebyte.sdk;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.devinebyte.sdk.diagnostics.Diagnostic;

public final class Builder {

    private Path projectRoot;
    private Path sourceDirectory;
    private Path outputDirectory;

    private boolean incremental;
    private boolean optimize;

    public Builder projectRoot(Path projectRoot) {
        this.projectRoot = projectRoot;
        return this;
    }

    public Builder sourceDirectory(Path sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        return this;
    }

    public Builder outputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public Builder incremental(boolean incremental) {
        this.incremental = incremental;
        return this;
    }

    public Builder optimize(boolean optimize) {
        this.optimize = optimize;
        return this;
    }

    public Session build() {

        CompilerContext context = new DefaultCompilerContext(projectRoot);

        return new Session(
                projectRoot,
                sourceDirectory,
                outputDirectory,
                incremental,
                optimize,
                context
        );
    }

    /**
     * Default compiler context used by SDK sessions.
     */
    private static final class DefaultCompilerContext implements CompilerContext {

        private final File workingDirectory;
        private final Map<String, String> options = new HashMap<>();
        private final List<Diagnostic> diagnostics = new ArrayList<>();

        DefaultCompilerContext(Path projectRoot) {
            this.workingDirectory = projectRoot.toFile();
        }

        @Override
        public File workingDirectory() {
            return workingDirectory;
        }

        @Override
        public Map<String, String> options() {
            return options;
        }

        @Override
        public List<Diagnostic> diagnostics() {
            return diagnostics;
        }
    }
}
