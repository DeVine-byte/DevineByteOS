package org.devinebyte.compiler.blueprint.validation;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

}
