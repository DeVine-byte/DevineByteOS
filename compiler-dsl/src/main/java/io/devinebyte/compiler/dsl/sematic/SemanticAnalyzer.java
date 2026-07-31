package io.devinebyte.compiler.dsl.semantic;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.ast.*;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class SemanticAnalyzer {

    public void analyze(CompilationContext context, List<AstNode> ast) {
        SymbolTable table = new SymbolTable();
        for (AstNode node : ast) {
            if (node instanceof EntityNode e) {
                if (table.isDefined(e.name())) {
                    context.diagnostics().addError("SEMANTIC_001", "Duplicate entity: " + e.name());
                }
                table.define(e.name(), "ENTITY");
            }
        }
        context.diagnostics().addInfo("SEMANTIC_COMPLETE", "Semantic analysis finished");
    }
}
