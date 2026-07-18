package org.devinebyte.compiler.core;

import java.util.ArrayList;
import java.util.List;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;
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

        this.codeGenerator = new JavaGenerator();
    }

    public CompilerPipelineResult compile() {

        try {

            ProjectModel project =
                    projectLoader.load(configuration);

            SourceProject sources =
                    sourceLoader.load(project);

            ProgramNode mergedProgram =
                    new ProgramNode();

            int tokenCount = 0;

            for (SourceFile file : sources.sourceFiles()) {

                List<Token> tokens =
                        lexer.tokenize(file.content());

                tokenCount += tokens.size();

                ProgramNode parsed =
                        parser.parse(tokens);

                mergedProgram.getDeclarations()
                        .addAll(parsed.getDeclarations());
            }

            SemanticResult semantic =
                    semanticAnalyzer.analyze(
                            mergedProgram,
                            configuration.context()
                    );

            if (!semantic.success()) {

                return CompilerPipelineResult.failure(
                        "Semantic analysis failed.",
                        configuration.context().diagnostics()
                );
            }

            BlueprintCompilationResult blueprint =
                    blueprintCompiler.compile(
                            semantic.model().program(),
                            configuration.context()
                    );

            if (!blueprint.success()) {

                return CompilerPipelineResult.failure(
                        "Blueprint compilation failed.",
                        configuration.context().diagnostics()
                );
            }

            GenerationResult generation =
                    codeGenerator.generate(
                            blueprint.blueprint()
                    );

            return CompilerPipelineResult.success(
                    sources.sourceFileCount(),
                    tokenCount,
                    mergedProgram.getDeclarations().size(),
                    generation.getGeneratedFiles(),
                    configuration.context().diagnostics()
            );

        } catch (Exception ex) {

            return CompilerPipelineResult.failure(
                    ex.getMessage(),
                    new ArrayList<>()
            );
        }
    }
}
