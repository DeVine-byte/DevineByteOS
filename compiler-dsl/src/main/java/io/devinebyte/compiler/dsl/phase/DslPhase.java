package io.devinebyte.compiler.dsl.phase;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
import io.devinebyte.compiler.dsl.KeywordDictionary; // NEW
import io.devinebyte.compiler.dsl.ast.AstNode;
import io.devinebyte.compiler.dsl.lexer.Lexer;
import io.devinebyte.compiler.dsl.lexer.Token;
import io.devinebyte.compiler.dsl.parser.Parser;
import io.devinebyte.compiler.dsl.semantic.SemanticAnalyzer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map; // NEW

@Singleton
public class DslPhase implements CompilerPhase {
    private final SemanticAnalyzer analyzer;
    private final KeywordDictionary keywordDictionary; // NEW

    @Inject
    public DslPhase(SemanticAnalyzer analyzer, KeywordDictionary keywordDictionary) { // NEW
        this.analyzer = analyzer;
        this.keywordDictionary = keywordDictionary;
    }

    @Override public String name() { return "dsl"; }

    @Override
    public CompilerResult<List<AstNode>> execute(CompilationContext ctx, CompilerResult input) throws Exception {
        String source = ctx.get("sourceCode"); // USE THIS FROM ORCHESTRATOR
        if (source == null) {
            Path dslPath = Path.of("samples/" + ctx.tenant().tenantId() + ".dbdsl");
            source = Files.readString(dslPath);
        }

        ctx.put("sourceCode", source);

        Map<String, String> aliases = ctx.get("keywordAliases");
        if (aliases == null) ctx.put("keywordAliases", Map.of());

        Lexer lexer = new Lexer(keywordDictionary);
        List<Token> tokens = lexer.scanTokens(ctx);
        System.out.println("DEBUG TOKENS: " + tokens.stream().map(t -> t.type() + ":" + t.lexeme()).toList());
        Parser parser = new Parser(tokens);
        List<AstNode> ast = parser.parse(ctx);

        analyzer.analyze(ctx, ast);
        ctx.put("ast", ast);
        return new CompilerResult<>(ctx.tenant(), ctx.diagnostics(), ast);
    }
}
