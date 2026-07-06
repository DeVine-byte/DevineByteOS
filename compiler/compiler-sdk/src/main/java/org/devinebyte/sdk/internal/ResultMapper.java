package org.devinebyte.sdk.internal;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import org.devinebyte.compiler.api.Result;

public final class ResultMapper {

    private ResultMapper() {
    }

    public static Result success() {
        return Result.success();
    }

}
