package org.devinebyte.compiler.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ProjectLoader {

    public ProjectModel load(CompilerConfiguration configuration) {

        Path projectRoot = configuration.projectRoot();

        if (!Files.exists(projectRoot)) {
            throw new IllegalArgumentException(
                    "Project root does not exist: " + projectRoot
            );
        }

        Path sourceDirectory = projectRoot.resolve("src");

        if (!Files.exists(sourceDirectory)) {
            throw new IllegalArgumentException(
                    "Missing src directory."
            );
        }

        List<Path> sourceFiles = new ArrayList<>();

        try {

            Files.walk(sourceDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString().endsWith(".dbos")
                                    || path.toString().endsWith(".bp"))
                    .forEach(sourceFiles::add);

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Unable to scan project.",
                    ex
            );
        }

        if (sourceFiles.isEmpty()) {

            throw new IllegalStateException(
                    "No source files found."
            );
        }

        return new ProjectModel(
                projectRoot,
                sourceDirectory,
                sourceFiles,
                configuration.projectName(),
                configuration.version()
        );
    }

}
