package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.lexer.DefaultLexer;
import org.devinebyte.compiler.parser.lexer.Lexer;
import org.devinebyte.compiler.parser.lexer.Token;
import org.devinebyte.compiler.parser.parser.DefaultParser;
import org.devinebyte.compiler.parser.parser.Parser;

import java.util.List;

/**
 * Coordinates the compiler pipeline.
 *
 * Current pipeline:
 *
 * CompilerConfiguration
 *        ↓
 * ProjectLoader
 *        ↓
 * ProjectModel
 *        ↓
 * SourceLoader
 *        ↓
 * SourceProject
 *        ↓
 * DefaultLexer
 *        ↓
 * DefaultParser
 *        ↓
 * ProgramNode (AST)
 *
 * Future stages:
 *
 * Semantic Analysis
 * Blueprint Compiler
 * Code Generator
 * Artifact Writer
 */
public final class CompilerEngine {

    private final CompilerConfiguration configuration;

    private final ProjectLoader projectLoader;
    private final SourceLoader sourceLoader;

    private final Lexer lexer;
    private final Parser parser;

    public CompilerEngine(CompilerConfiguration configuration) {

        this.configuration = configuration;

        this.projectLoader = new ProjectLoader();
        this.sourceLoader = new SourceLoader();

        this.lexer = new DefaultLexer();
        this.parser = new DefaultParser();
    }

    /**
     * Executes the compiler pipeline.
     */
    public CompilationResult compile() {

        try {

            // Stage 1
            ProjectModel project =
                    projectLoader.load(configuration);

            // Stage 2
            SourceProject sources =
                    sourceLoader.load(project);

            int tokenCount = 0;

            ProgramNode lastProgram = null;

            // Stage 3 + 4
            for (SourceFile file : sources.sourceFiles()) {

                List<Token> tokens =
                        lexer.tokenize(file.contents());

                tokenCount += tokens.size();

                lastProgram =
                        parser.parse(file.contents());
            }

            return new CompilationResult(
                    true,
                    "Loaded "
                            + sources.sourceFileCount()
                            + " source file(s), "
                            + tokenCount
                            + " token(s), "
                            + (lastProgram == null
                                    ? 0
                                    : lastProgram.getDeclarations().size())
                            + " declaration(s).",
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
