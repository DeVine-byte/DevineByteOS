package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;

public final class CompilerEngine {

    private final CompilerConfiguration configuration;
    private final ProjectLoader loader;

    public CompilerEngine(
            CompilerConfiguration configuration
    ) {
        this.configuration = configuration;
        this.loader = new ProjectLoader();
    }

    public CompilationResult compile() {

        try {

            ProjectModel project =
                    loader.load(configuration);

            return new CompilationResult(
                    true,
                    "Project loaded: "
                            + project.sourceFileCount()
                            + " source files.",
                    null
            );

        } catch (Exception ex) {

            return new CompilationResult(
                    false,
                    null,
                    ex.getMessage()
            );
        }
    }

}
