package org.devinebyte.compiler.api.internal;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.Result;

public final class ResultMapper {

    private ResultMapper() {
    }

    public static Result success() {
        return Result.success();
    }

}
