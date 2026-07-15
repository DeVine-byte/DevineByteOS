package org.devinebyte.compiler.core;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Immutable representation of a discovered DevineByte project.
 */
public final class ProjectModel {

    private final Path projectRoot;
    private final Path sourceDirectory;
    private final List<Path> sourceFiles;
    private final String projectName;
    private final String version;

    public ProjectModel(
            Path projectRoot,
            Path sourceDirectory,
            List<Path> sourceFiles,
            String projectName,
            String version
    ) {

        this.projectRoot = Objects.requireNonNull(projectRoot);
        this.sourceDirectory = Objects.requireNonNull(sourceDirectory);
        this.sourceFiles = List.copyOf(sourceFiles);

        this.projectName =
                projectName == null
                        ? "unnamed"
                        : projectName;

        this.version =
                version == null
                        ? "0.0.1"
                        : version;
    }

    public Path projectRoot() {
        return projectRoot;
    }

    public Path sourceDirectory() {
        return sourceDirectory;
    }

    public List<Path> sourceFiles() {
        return sourceFiles;
    }

    public String projectName() {
        return projectName;
    }

    public String version() {
        return version;
    }

    public int sourceFileCount() {
        return sourceFiles.size();
    }

}
