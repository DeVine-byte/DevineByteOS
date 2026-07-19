package org.devinebyte.compiler.blueprint.validation;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;

import java.util.Objects;

/**
 * Default implementation of the blueprint validator.
 *
 * Performs structural validation on the compiled blueprint
 * before code generation begins.
 *
 * Current validations:
 * - blueprint is not null
 *
 * Future validations:
 * - duplicate entities
 * - duplicate workflows
 * - invalid references
 * - cyclic dependencies
 * - module validation
 * - runtime constraints
 */
public final class DefaultBlueprintValidator
        implements BlueprintValidator {

    @Override
    public ValidationResult validate(BlueprintModel blueprint) {

        ValidationResult result = new ValidationResult();

        if (Objects.isNull(blueprint)) {
            result.getErrors().add("Blueprint cannot be null.");
            return result;
        }

        /*
         * Future validation rules are added here.
         */

        return result;
    }
        }
