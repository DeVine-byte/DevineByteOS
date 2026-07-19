package org.devinebyte.compiler.generator.engine;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerationResult {

    private final List<Path> generatedFiles = new ArrayList<>();

    public void add(Path file) {
        generatedFiles.add(file);
    }

    public List<Path> getGeneratedFiles() {
        return List.copyOf(generatedFiles);
    }

}
