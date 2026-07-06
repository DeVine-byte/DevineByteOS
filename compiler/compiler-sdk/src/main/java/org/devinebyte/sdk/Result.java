package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.util.Collections;
import java.util.List;

public final class Result {

    private final boolean success;
    private final List<String> diagnostics;

    private Result(boolean success, List<String> diagnostics) {
        this.success = success;
        this.diagnostics = diagnostics;
    }

    public static Result success() {
        return new Result(true, Collections.emptyList());
    }

    public static Result failure(List<String> diagnostics) {
        return new Result(false, diagnostics);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getDiagnostics() {
        return diagnostics;
    }

}
