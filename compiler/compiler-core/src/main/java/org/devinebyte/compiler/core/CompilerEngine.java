package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilationResult;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompiler;
import org.devinebyte.compiler.blueprint.compiler.BlueprintCompilerImpl;
import org.devinebyte.compiler.blueprint.mapper.AstToBlueprintMapper;
import org.devinebyte.compiler.blueprint.validation.DefaultBlueprintValidator;
import org.devinebyte.compiler.generator.engine.CodeGenerator;
import org.devinebyte.compiler.generator.engine.GenerationResult;
import org.devinebyte.compiler.generator.java.JavaGenerator;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.parser.lexer.DefaultLexer;
import org.devinebyte.compiler.parser.lexer.Token;
import org.devinebyte.compiler.parser.parser.DefaultParser;
import org.devinebyte.compiler.parser.semantic.DefaultSemanticAnalyzer;
import org.devinebyte.compiler.parser.semantic.SemanticResult;

import java.util.List;

public final class CompilerEngine {

    private final CompilerConfiguration configuration;
    private final CompilerContext context;

    private final ProjectLoader projectLoader;
    private final SourceLoader sourceLoader;

    private final DefaultLexer lexer;
    private final DefaultParser parser;
    private final DefaultSemanticAnalyzer semanticAnalyzer;

    private final BlueprintCompiler blueprintCompiler;
    private final CodeGenerator codeGenerator;

    public CompilerEngine(
            CompilerConfiguration configuration,
            CompilerContext context
    ) {

        this.configuration = configuration;
        this.context = context;

        this.projectLoader = new ProjectLoader();
        this.sourceLoader = new SourceLoader();

        this.lexer = new DefaultLexer();
        this.parser = new DefaultParser();
        this.semanticAnalyzer = new DefaultSemanticAnalyzer();

        this.blueprintCompiler =
                new BlueprintCompilerImpl(
                        new AstToBlueprintMapper(),
                        new DefaultBlueprintValidator()
                );

        this.codeGenerator =
                new JavaGenerator();
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

                mergedProgram
                        .getDeclarations()
                        .addAll(parsed.getDeclarations());
            }

            SemanticResult semantic =
                    semanticAnalyzer.analyze(
                            mergedProgram,
                            context
                    );

            if (!semantic.success()) {

                return CompilerPipelineResult.failure(
                        "Semantic analysis failed.",
                        context.diagnostics()
                );
            }

            BlueprintCompilationResult blueprint =
                    blueprintCompiler.compile(
                            semantic.model().program(),
                            context
                    );

            if (!blueprint.success()) {

                return CompilerPipelineResult.failure(
                        "Blueprint compilation failed.",
                        context.diagnostics()
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
                    context.diagnostics()
            );

        } catch (Exception ex) {

            return CompilerPipelineResult.failure(
                    ex.getMessage(),
                    context == null
                            ? List.of()
                            : context.diagnostics()
            );
        }
    }
}
