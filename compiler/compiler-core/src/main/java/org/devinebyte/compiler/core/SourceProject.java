package org.devinebyte.compiler.core;

import java.util.List;
import java.util.Objects;

/**
 * Represents a fully loaded project ready for lexing.
 *
 * <p>A SourceProject consists of the discovered project metadata
 * together with every source file loaded into memory.</p>
 */
public final class SourceProject {

    private final ProjectModel project;
    private final List<SourceFile> sourceFiles;

    public SourceProject(
            ProjectModel project,
            List<SourceFile> sourceFiles
    ) {
        this.project = Objects.requireNonNull(project, "project");
        this.sourceFiles = List.copyOf(
                Objects.requireNonNull(sourceFiles, "sourceFiles")
        );
    }

    /**
     * Returns the discovered project model.
     */
    public ProjectModel project() {
        return project;
    }

    /**
     * Returns every loaded source file.
     */
    public List<SourceFile> sourceFiles() {
        return sourceFiles;
    }

    /**
     * Returns the total number of source files.
     */
    public int sourceFileCount() {
        return sourceFiles.size();
    }

    /**
     * Returns true if no source files were loaded.
     */
    public boolean isEmpty() {
        return sourceFiles.isEmpty();
    }

    @Override
    public String toString() {
        return "SourceProject{" +
                "project=" + project.projectName() +
                ", version='" + project.version() + '\'' +
                ", sourceFiles=" + sourceFileCount() +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, sourceFiles);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SourceProject other)) {
            return false;
        }

        return project.equals(other.project)
                && sourceFiles.equals(other.sourceFiles);
    }
}
