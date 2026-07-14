package org.devinebyte.compiler.core;

import java.nio.file.Path;
import java.util.Objects;

public record CompilerConfiguration(
        Path projectRoot,
        Path sourceDirectory,
        Path outputDirectory,
        boolean incremental,
        boolean optimize
) {

    public CompilerConfiguration {
        Objects.requireNonNull(projectRoot, "projectRoot");
        Objects.requireNonNull(sourceDirectory, "sourceDirectory");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
    }
}
