package org.devinebyte.compiler.blueprint.validation;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;

public interface BlueprintValidator {

    ValidationResult validate(BlueprintModel model);

}
