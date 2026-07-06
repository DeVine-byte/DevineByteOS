package org.devinebyte.compiler.blueprint.compiler;

import org.devinebyte.compiler.blueprint.model.BlueprintModel;
import org.devinebyte.sdk.diagnostics.Diagnostic;
import java.util.List;

public record BlueprintCompilationResult(
    BlueprintModel blueprint,
    boolean success,
    List<Diagnostic> diagnostics
) {
    public static BlueprintCompilationResult success(BlueprintModel blueprint, List<Diagnostic> diagnostics) {
        return new BlueprintCompilationResult(blueprint, true, diagnostics);
    }

    public static BlueprintCompilationResult failed(BlueprintModel blueprint, List<Diagnostic> diagnostics) {
        return new BlueprintCompilationResult(blueprint, false, diagnostics);
    }
}
