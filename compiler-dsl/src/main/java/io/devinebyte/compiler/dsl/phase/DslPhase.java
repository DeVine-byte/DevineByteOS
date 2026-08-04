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
        Path dslPath = Path.of("samples/" + ctx.tenant().tenantId() + ".dbdsl");
        String source = Files.readString(dslPath);

        // NEW: Put source into context so Lexer can read it
        ctx.put("sourceCode", source);
        
        // NEW: Load aliases if they exist. If not, Lexer will use base keywords
        @SuppressWarnings("unchecked")
        Map<String, String> aliases = ctx.get("keywordAliases");
        if (aliases == null) {
            ctx.put("keywordAliases", Map.of()); // ensure empty map so dict.reset() runs
        }

        // CHANGED: Lexer now takes KeywordDictionary, not source
        Lexer lexer = new Lexer(keywordDictionary);
        List<Token> tokens = lexer.scanTokens(ctx); // aliases loaded inside

        Parser parser = new Parser(tokens);
        List<AstNode> ast = parser.parse(ctx);

        analyzer.analyze(ctx, ast);
        ctx.put("ast", ast);
        return new CompilerResult<>(ctx.tenant(), ctx.diagnostics(), ast);
    }
}
