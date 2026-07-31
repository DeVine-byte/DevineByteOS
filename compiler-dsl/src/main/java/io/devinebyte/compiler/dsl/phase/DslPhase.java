package io.devinebyte.compiler.dsl.phase;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.core.pipeline.CompilerPhase;
import io.devinebyte.compiler.core.pipeline.CompilerResult;
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

@Singleton 
public class DslPhase implements CompilerPhase {
    private final SemanticAnalyzer analyzer;

    @Inject 
    public DslPhase(SemanticAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override public String name() { return "dsl"; }
    
    @Override
    public CompilerResult<List<AstNode>> execute(CompilationContext ctx, CompilerResult input) throws Exception {
        Path dslPath = Path.of("samples/" + ctx.tenant().tenantId() + ".dbdsl");
        String source = Files.readString(dslPath);

        Lexer lexer = new Lexer(source); // <-- constructor takes source
        List<Token> tokens = lexer.scanTokens(ctx); // <-- method is scanTokens
        
        Parser parser = new Parser(tokens); // <-- constructor takes tokens
        List<AstNode> ast = parser.parse(ctx); // <-- only takes context
        
        analyzer.analyze(ctx, ast); 
        ctx.put("ast", ast); 
        return new CompilerResult<>(ctx.tenant(), ctx.diagnostics(), ast);
    }
}
