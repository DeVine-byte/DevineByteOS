package io.devinebyte.compiler.dsl.type;

import io.devinebyte.compiler.core.context.CompilationContext;
import io.devinebyte.compiler.dsl.ast.AstNode;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TypeChecker {
    public void check(CompilationContext context, List<AstNode> ast) {
        // TODO: Validate field types, event payloads, workflow step references
        context.diagnostics().addInfo("TYPECHECK_COMPLETE", "Type checking finished");
    }
}
