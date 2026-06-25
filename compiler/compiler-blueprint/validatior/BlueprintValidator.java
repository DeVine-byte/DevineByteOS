package com.devinebyte.compiler.blueprint.validator;

import com.devinebyte.compiler.blueprint.model.BlueprintModel;

public interface BlueprintValidator {

    ValidationResult validate(BlueprintModel model);

}
