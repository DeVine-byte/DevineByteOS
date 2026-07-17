package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;

/**
 * Coordinates the compiler pipeline.
 *
 * <p>Current pipeline:</p>
 *
 * <pre>
 * CompilerConfiguration
 *          ↓
 * ProjectLoader
 *          ↓
 * ProjectModel
 *          ↓
 * SourceLoader
 *          ↓
 * SourceProject
 * </pre>
 *
 * <p>Future stages (Lexer, Parser, Semantic Analysis,
 * Blueprint Compilation, Code Generation) will be added
 * incrementally.</p>
 */
public final class CompilerEngine {

    private final CompilerConfiguration configuration;
    private final ProjectLoader projectLoader;
    private final SourceLoader sourceLoader;

    public CompilerEngine(CompilerConfiguration configuration) {
        this.configuration = configuration;
        this.projectLoader = new ProjectLoader();
        this.sourceLoader = new SourceLoader();
    }

    /**
     * Executes the compiler pipeline.
     *
     * @return compilation result
     */
    public CompilationResult compile() {

        try {

            // Stage 1: Discover project
            ProjectModel project =
                    projectLoader.load(configuration);

            // Stage 2: Load source files
            SourceProject sources =
                    sourceLoader.load(project);

            return new CompilationResult(
                    true,
                    "Loaded "
                            + sources.sourceFileCount()
                            + " source file(s).",
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
