package org.devinebyte.compiler.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads every discovered source file into memory.
 *
 * <p>This stage sits between ProjectLoader and the Lexer.
 * It converts a ProjectModel (paths only) into a SourceProject
 * containing the file contents ready for lexing.</p>
 */
public final class SourceLoader {

    /**
     * Loads all source files belonging to the project.
     *
     * @param project discovered project model
     * @return loaded source project
     */
    public SourceProject load(ProjectModel project) {

        List<SourceFile> loadedFiles = new ArrayList<>();

        for (Path file : project.sourceFiles()) {
            loadedFiles.add(loadFile(file));
        }

        return new SourceProject(project, loadedFiles);
    }

    /**
     * Loads a single source file.
     */
    private SourceFile loadFile(Path path) {

        try {

            String content = Files.readString(path);

            return new SourceFile(
                    path,
                    content
            );

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Failed to read source file: " + path,
                    ex
            );

        }
    }
}
