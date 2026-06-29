package org.devinebyte.compiler.blueprint.validator;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;

public interface BlueprintValidator {

    ValidationResult validate(BlueprintModel model);

}
