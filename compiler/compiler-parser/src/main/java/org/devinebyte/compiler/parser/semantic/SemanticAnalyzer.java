package org.devinebyte.compiler.parser.semantic;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;
import org.devinebyte.compiler.parser.ast.EntityNode;
import org.devinebyte.compiler.parser.ast.ProgramNode;

public class SemanticAnalyzer {

    public void analyze(ProgramNode program, CompilerContext ctx) {
        SymbolTable table = new SymbolTable();

        for (var node : program.getDeclarations()) {

            if (!(node instanceof EntityNode entity)) {
                continue;
            }

            if (table.isDeclared(entity.getName())) {

                ctx.diagnostics().add(
                        new Diagnostic(
                                DiagnosticSeverity.ERROR,
                                "SEM001",
                                "Duplicate entity: " + entity.getName(),
                                0,
                                0
                        )
                );

            } else {

                table.define(entity.getName(), entity);

            }
        }
    }
}
