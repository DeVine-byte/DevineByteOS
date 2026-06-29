package org.devinebyte.compiler.parser.semantic;

import org.devinebyte.compiler.parser.ast.ProgramNode;

public interface SemanticAnalyzer {

    void analyze(ProgramNode program);

}
