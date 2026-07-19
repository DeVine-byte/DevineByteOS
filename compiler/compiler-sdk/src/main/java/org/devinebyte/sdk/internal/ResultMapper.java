package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilerPipelineResult;
import org.devinebyte.sdk.Result;

/**
 * Maps the internal compiler pipeline result into the public SDK result.
 */
public final class ResultMapper {

    private ResultMapper() {
    }

    public static Result map(CompilerPipelineResult pipeline) {

        if (pipeline == null) {
            return Result.failed(
                    java.util.List.of("Compiler returned no result.")
            );
        }

        if (!pipeline.success()) {

            if (pipeline.hasDiagnostics()) {

                return Result.failed(
                        pipeline.diagnostics()
                                .stream()
                                .map(Object::toString)
                                .toList()
                );
            }

            return Result.failed(
                    java.util.List.of(
                            pipeline.message() == null
                                    ? "Compilation failed."
                                    : pipeline.message()
                    )
            );
        }

        return Result.successful(
                pipeline.artifacts()
        );
    }
}
