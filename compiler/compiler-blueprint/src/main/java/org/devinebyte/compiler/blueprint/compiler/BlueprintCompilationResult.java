package org.devinebyte.compiler.blueprint.compiler;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;

public record BlueprintCompilationResult(

        BlueprintModel blueprint,

        boolean success

) {}
