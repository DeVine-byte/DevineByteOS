package org.devinebyte.compiler.parser.semantic;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable semantic analysis result.
 */
public final class SemanticResult {

    private final SemanticModel model;
    private final List<Diagnostic> diagnostics;
    private final boolean success;

    public SemanticResult(
            SemanticModel model,
            List<Diagnostic> diagnostics,
            boolean success
    ) {
        this.model = model;
        this.diagnostics = diagnostics == null
                ? List.of()
                : List.copyOf(diagnostics);
        this.success = success;
    }

    public SemanticModel model() {
        return model;
    }

    public List<Diagnostic> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public boolean success() {
        return success;
    }
}
