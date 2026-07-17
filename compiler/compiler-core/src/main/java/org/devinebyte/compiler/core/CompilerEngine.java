package org.devinebyte.compiler.core;

import java.util.List;

import org.devinebyte.compiler.api.CompilationResult;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilationResult;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompiler;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.generator.engine.CodeGenerator;
import org.devinebyte.compiler.generator.engine.GenerationResult;
import org.devinebyte.compiler.generator.java.JavaGenerator;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.lexer.DefaultLexer;
import org.devinebyte.compiler.parser.lexer.Token;
import org.devinebyte.compiler.parser.parser.DefaultParser;
import org.devinebyte.compiler.parser.semantic.DefaultSemanticAnalyzer;
import org.devinebyte.compiler.parser.semantic.SemanticResult;

public final class CompilerEngine {

    private final CompilerConfiguration configuration;

    private final ProjectLoader projectLoader;
    private final SourceLoader sourceLoader;

    private final DefaultLexer lexer;
    private final DefaultParser parser;
    private final DefaultSemanticAnalyzer semanticAnalyzer;

    private final BlueprintCompiler blueprintCompiler;
    private final CodeGenerator codeGenerator;

    public CompilerEngine(
            CompilerConfiguration configuration
    ) {

        this.configuration = configuration;

        this.projectLoader = new ProjectLoader();
        this.sourceLoader = new SourceLoader();

        this.lexer = new DefaultLexer();
        this.parser = new DefaultParser();
        this.semanticAnalyzer = new DefaultSemanticAnalyzer();

        this.blueprintCompiler =
                new BlueprintCompilerImpl(
                        new AstToBlueprintMapper(),
                        new BlueprintValidator()
                );

        this.codeGenerator =
                new JavaGenerator();
    }

    public CompilationResult compile() {

        try {

            // ----------------------------------------------------
            // Stage 1
            // ----------------------------------------------------

            ProjectModel project =
                    projectLoader.load(configuration);

            // ----------------------------------------------------
            // Stage 2
            // ----------------------------------------------------

            SourceProject sources =
                    sourceLoader.load(project);

            // ----------------------------------------------------
            // Stage 3
            // ----------------------------------------------------

            ProgramNode mergedProgram =
                    new ProgramNode();

            for (SourceFile file : sources.sourceFiles()) {

                List<Token> tokens =
                        lexer.tokenize(file.content());

                ProgramNode parsed =
                        parser.parse(tokens);

                mergedProgram
                        .getDeclarations()
                        .addAll(parsed.getDeclarations());
            }

            // ----------------------------------------------------
            // Stage 4
            // ----------------------------------------------------

            SemanticResult semantic =
                    semanticAnalyzer.analyze(
                            mergedProgram,
                            configuration.context()
                    );

            if (!semantic.success()) {

                return new CompilationResult(
                        false,
                        null,
                        "Semantic analysis failed."
                );
            }

            // ----------------------------------------------------
            // Stage 5
            // ----------------------------------------------------

            BlueprintCompilationResult blueprint =
                    blueprintCompiler.compile(
                            semantic.model().program(),
                            configuration.context()
                    );

            if (!blueprint.success()) {

                return new CompilationResult(
                        false,
                        null,
                        "Blueprint compilation failed."
                );
            }

            // ----------------------------------------------------
            // Stage 6
            // ----------------------------------------------------

            GenerationResult generation =
                    codeGenerator.generate(
                            blueprint.blueprint()
                    );

            return new CompilationResult(
                    true,
                    "Compilation completed successfully. Generated "
                            + generation.getGeneratedFiles().size()
                            + " file(s).",
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
