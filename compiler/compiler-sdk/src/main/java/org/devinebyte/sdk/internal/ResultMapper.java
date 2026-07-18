package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.compiler.core.CompilerPipelineResult;
import org.devinebyte.sdk.Result;

import java.util.List;

/**
 * Converts the internal compiler pipeline result into the public SDK result.
 */
public final class ResultMapper {

    private ResultMapper() {
    }

    public static Result map(CompilerPipelineResult pipeline) {

        if (pipeline == null) {
            return Result.failed(
                    List.of("Compiler returned null pipeline result.")
            );
        }

        if (!pipeline.success()) {

            List<String> diagnostics =
                    pipeline.diagnostics()
                            .stream()
                            .map(Diagnostic::message)
                            .toList();

            if (diagnostics.isEmpty()) {
                diagnostics = List.of(
                        pipeline.message() == null
                                ? "Compilation failed."
                                : pipeline.message()
                );
            }

            return Result.failed(diagnostics);
        }

        return Result.successful(
                pipeline.artifacts()
        );
    }
}
