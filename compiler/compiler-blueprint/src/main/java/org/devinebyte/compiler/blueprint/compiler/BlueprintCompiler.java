package org.devinebyte.compiler.blueprint.compiler;

import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.sdk.CompilerContext;

public interface BlueprintCompiler {
    BlueprintCompilationResult compile(ProgramNode program, CompilerContext context);
}
