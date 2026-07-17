package org.devinebyte.compiler.parser.semantic;

import org.devinebyte.compiler.parser.ast.ProgramNode;

import java.util.Objects;

/**
 * Result of semantic analysis.
 *
 * <p>Contains the validated AST together with the symbol table
 * built during semantic analysis.</p>
 */
public final class SemanticModel {

    private final ProgramNode program;
    private final SymbolTable symbolTable;

    public SemanticModel(
            ProgramNode program,
            SymbolTable symbolTable
    ) {
        this.program = Objects.requireNonNull(program, "program");
        this.symbolTable = Objects.requireNonNull(symbolTable, "symbolTable");
    }

    public ProgramNode program() {
        return program;
    }

    public SymbolTable symbolTable() {
        return symbolTable;
    }
}
