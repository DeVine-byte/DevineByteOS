package org.devinebyte.compiler.blueprint.compiler;

import org.devinebyte.compiler.parser.ast.ProgramNode;

public interface BlueprintCompiler {

    BlueprintCompilationResult compile(ProgramNode program);

}
