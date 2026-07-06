package org.devinebyte.compiler.blueprint.compiler;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.blueprint.validation.BlueprintValidator;
import org.devinebyte.compiler.parser.ast.ProgramNode;
import org.devinebyte.compiler.api.CompilerContext;

public interface BlueprintCompiler {
    BlueprintCompilationResult compile(ProgramNode program, CompilerContext context);
}
