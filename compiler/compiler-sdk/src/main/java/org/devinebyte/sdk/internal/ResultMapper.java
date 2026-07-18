package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.api.CompilationResult;
import org.devinebyte.sdk.Result;

import java.util.Collections;
import java.util.List;

/**
 * Converts compiler results into SDK results.
 */
public final class ResultMapper {

    private ResultMapper() {
    }

    public static Result map(CompilationResult compilationResult) {

        if (compilationResult == null) {
            return Result.failed(
                    List.of("Compiler returned null result.")
            );
        }

        if (!compilationResult.success()) {

            String error = compilationResult.error();

            return Result.failed(
                    error == null
                            ? List.of("Compilation failed.")
                            : List.of(error)
            );
        }

        return Result.successful(
                Collections.emptyList()
        );
    }
}
