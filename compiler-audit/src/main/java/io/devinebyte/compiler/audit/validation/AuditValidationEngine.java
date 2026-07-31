package io.devinebyte.compiler.audit.validation;

import io.devinebyte.compiler.audit.model.AuditModel;
import io.devinebyte.compiler.core.context.CompilationContext;
import jakarta.inject.Singleton;

@Singleton
public class AuditValidationEngine {

    public void validate(CompilationContext context, AuditModel model) {
        var diagnostics = context.diagnostics();
        
        if (model.companyName() == null || model.companyName().isBlank()) {
            diagnostics.addError("VALIDATION_001", "Company name is required");
        }
        
        if (model.businessUnits().isEmpty()) {
            diagnostics.addError("VALIDATION_002", "At least one BusinessUnit is required");
        }
        
        model.businessUnits().forEach(bu -> {
            if (bu.id() == null || bu.id().isBlank()) {
                diagnostics.addError("VALIDATION_003", "BusinessUnit id cannot be empty");
            }
        });
        
        model.processes().forEach(p -> {
            if (p.businessUnitId() == null) {
                diagnostics.addError("VALIDATION_004", "Process " + p.id() + " must have businessUnitId");
            }
        });
        
        diagnostics.addInfo("VALIDATION_COMPLETE", "Audit validation finished");
    }
}
