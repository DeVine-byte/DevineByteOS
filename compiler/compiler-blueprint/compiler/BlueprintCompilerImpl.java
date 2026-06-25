package com.devinebyte.compiler.blueprint.compiler;

import com.devinebyte.compiler.parser.ast.ProgramNode;

public class BlueprintCompilerImpl implements BlueprintCompiler {

    @Override
    public BlueprintCompilationResult compile(ProgramNode program) {

        BlueprintModel model = new BlueprintModel();

        return new BlueprintCompilationResult(model, true);

    }

}
