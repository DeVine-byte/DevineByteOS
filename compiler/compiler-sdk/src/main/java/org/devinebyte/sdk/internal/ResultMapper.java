package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.api.CompilationResult;
import org.devinebyte.sdk.Result;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Maps compiler CompilationResult -> SDK Result.
 */
@Deprecated(forRemoval = true);
public final class ResultMapper {

    private ResultMapper() {
    }

    public static Result map(CompilationResult compilationResult) {

        if (compilationResult.success()) {
            return Result.successful(Collections.<Path>emptyList());
        }

        String error = compilationResult.error();

        return Result.failed(
                error == null
                        ? Collections.emptyList()
                        : List.of(error)
        );
    }
}
