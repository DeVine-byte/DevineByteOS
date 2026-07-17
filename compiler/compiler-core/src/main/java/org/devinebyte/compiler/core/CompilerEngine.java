package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilationResult;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilationResult;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompiler;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.lexer.DefaultLexer;
import org.devinebyte.compiler.parser.lexer.Token;
import org.devinebyte.compiler.parser.parser.DefaultParser;
import org.devinebyte.compiler.parser.semantic.DefaultSemanticAnalyzer;
import org.devinebyte.compiler.parser.semantic.SemanticResult;

import java.util.List;

/**
 * Coordinates the complete compiler pipeline.
 */
public final class CompilerEngine {

    private final CompilerConfiguration configuration;

    private final ProjectLoader projectLoader;
    private final SourceLoader sourceLoader;

    private final DefaultLexer lexer;
    private final DefaultParser parser;
    private final DefaultSemanticAnalyzer semanticAnalyzer;

    private final BlueprintCompiler blueprintCompiler;

    public CompilerEngine(CompilerConfiguration configuration) {

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
    }

    public CompilationResult compile() {

        try {

            ProjectModel project =
                    projectLoader.load(configuration);

            SourceProject sources =
                    sourceLoader.load(project);

            ProgramNode program =
                    new ProgramNode();

            for (SourceFile file : sources.sourceFiles()) {

                List<Token> tokens =
                        lexer.tokenize(file.content());

                ProgramNode parsed =
                        parser.parse(tokens);

                program.getDeclarations()
                        .addAll(parsed.getDeclarations());
            }

            SemanticResult semantic =
                    semanticAnalyzer.analyze(
                            program,
                            configuration.context()
                    );

            if (!semantic.success()) {

                return new CompilationResult(
                        false,
                        null,
                        "Semantic analysis failed."
                );
            }

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

            return new CompilationResult(
                    true,
                    "Blueprint compilation completed successfully.",
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
