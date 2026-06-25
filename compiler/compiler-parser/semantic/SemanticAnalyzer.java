package com.devinebyte.compiler.parser.semantic;

import com.devinebyte.compiler.parser.ast.ProgramNode;

public interface SemanticAnalyzer {

    void analyze(ProgramNode program);

}
