package org.devinebyte.compiler.generator.engine;

import com.devinebyte.compiler.blueprint.model.BlueprintModel;

public interface CodeGenerator {

    GenerationResult generate(BlueprintModel blueprint);

}
