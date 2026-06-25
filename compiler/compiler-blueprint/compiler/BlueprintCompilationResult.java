package com.devinebyte.compiler.blueprint.compiler;

import com.devinebyte.compiler.blueprint.model.BlueprintModel;

public record BlueprintCompilationResult(

        BlueprintModel blueprint,

        boolean success

) {}
