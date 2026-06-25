package org.devinebyte.compiler.blueprint.compiler;

import com.devinebyte.compiler.parser.ast.ProgramNode;

public interface BlueprintCompiler {

    BlueprintCompilationResult compile(ProgramNode program);

}
