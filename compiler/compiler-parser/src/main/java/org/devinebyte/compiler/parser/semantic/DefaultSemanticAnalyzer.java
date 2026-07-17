package org.devinebyte.compiler.parser.semantic;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;
import org.devinebyte.compiler.parser.ast.AstNode;
import org.devinebyte.compiler.parser.ast.EntityNode;
import org.devinebyte.compiler.parser.ast.ProgramNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of semantic analysis.
 *
 * Current validations:
 *
 * - duplicate entity names
 *
 * Future:
 *
 * - duplicate workflows
 * - duplicate properties
 * - type resolution
 * - reference resolution
 * - inheritance
 * - module validation
 */
public final class DefaultSemanticAnalyzer {

    public SemanticResult analyze(
            ProgramNode program,
            CompilerContext context
    ) {

        Objects.requireNonNull(program, "program");
        Objects.requireNonNull(context, "context");

        SymbolTable symbolTable = new SymbolTable();

        List<Diagnostic> diagnostics = new ArrayList<>();

        for (AstNode node : program.getDeclarations()) {

            if (!(node instanceof EntityNode entity)) {
                continue;
            }

            String name = entity.getName();

            if (symbolTable.isDeclared(name)) {

                Diagnostic diagnostic =
                        new Diagnostic(
                                DiagnosticSeverity.ERROR,
                                "SEM001",
                                "Duplicate entity: " + name,
                                0,
                                0
                        );

                diagnostics.add(diagnostic);

                context.diagnostics().add(diagnostic);

                continue;
            }

            symbolTable.define(name, entity);
        }

        SemanticModel model =
                new SemanticModel(
                        program,
                        symbolTable
                );

        return new SemanticResult(
                model,
                diagnostics,
                diagnostics.isEmpty()
        );
    }
}
