package org.devinebyte.compiler.core;

import org.devinebyte.compiler.blueprint.compiler.BlueprintCompiler;
import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.compiler.generator.engine.CodeGenerator;
import org.devinebyte.compiler.generator.engine.GenerationResult;
import org.devinebyte.compiler.generator.engine.SourceWriter;
import org.devinebyte.compiler.lexing.Lexer;
import org.devinebyte.compiler.parser.Parser;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.semantic.SemanticAnalyzer;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.CompilerResult;
import org.devinebyte.sdk.diagnostics.Severity;

import java.nio.file.Path;
import java.util.List;

public final class CompilerEngine {
    private final Lexer lexer;
    private final Parser parser;
    private final SemanticAnalyzer semanticAnalyzer;
    private final BlueprintCompiler blueprintCompiler;
    private final CodeGenerator generator;
    private final SourceWriter writer;

    public CompilerEngine(
            Lexer lexer, Parser parser, SemanticAnalyzer semanticAnalyzer,
            BlueprintCompiler blueprintCompiler, CodeGenerator generator, SourceWriter writer) {
        this.lexer = lexer;
        this.parser = parser;
        this.semanticAnalyzer = semanticAnalyzer;
        this.blueprintCompiler = blueprintCompiler;
        this.generator = generator;
        this.writer = writer;
    }

    public CompilerResult compile(Path sourceFile, CompilerContext ctx) {
        var tokens = lexer.lex(sourceFile, ctx.diagnostics());
        if (ctx.diagnostics().hasSeverity(Severity.ERROR)) return CompilerResult.failed(ctx.diagnostics().all());

        ProgramNode ast = parser.parse(tokens, ctx.diagnostics());
        if (ctx.diagnostics().hasSeverity(Severity.ERROR)) return CompilerResult.failed(ctx.diagnostics().all());
        semanticAnalyzer.analyze(ast, ctx);
        if (ctx.diagnostics().hasSeverity(Severity.ERROR)) return CompilerResult.failed(ctx.diagnostics().all());

        var bpResult = blueprintCompiler.compile(ast, ctx);
        if (!bpResult.success()) return CompilerResult.failed(bpResult.diagnostics());

        GenerationResult genResult = generator.generate(bpResult.model(), ctx);
        List<Path> artifacts = writer.write(genResult.generatedFiles(), ctx.workingDirectory(), ctx.diagnostics());

        return new CompilerResult(artifacts, ctx.diagnostics().all());
    }
}
