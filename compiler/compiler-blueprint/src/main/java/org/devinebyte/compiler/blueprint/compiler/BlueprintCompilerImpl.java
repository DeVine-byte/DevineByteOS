package org.devinebyte.compiler.blueprint.compiler;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.compiler.parser.ast.ProgramNode;

public class BlueprintCompilerImpl implements BlueprintCompiler {

    @Override
    public BlueprintCompilationResult compile(ProgramNode program) {
        BlueprintModel model = new BlueprintModel();
        return new BlueprintCompilationResult(model, true);
    }

    public BlueprintModel compile(BlueprintModel model) {
        // TODO: Implement logic
        return model;
    }
}
