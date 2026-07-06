package org.devinebyte.compiler.semantic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.Severity;

public class SemanticAnalyzer {

    public void analyze(ProgramNode program, CompilerContext ctx) {
        SymbolTable table = new SymbolTable();

        // Example: check entities for duplicates. Replace with real AST walk.
        for (var entity : program.entities()) {
            if (table.isDeclared(entity.name())) {
                ctx.diagnostics().add(new Diagnostic(
                    DiagnosticSeverity.ERROR, "SEM001",
                    "Duplicate entity: " + entity.name(),
                    entity.location()
                ));
            } else {
                table.define(entity.name(), entity);
            }
        }

        // TODO: Add type resolution, reference checks, etc.
    }
}
