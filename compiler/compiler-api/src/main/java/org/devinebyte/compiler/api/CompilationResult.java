package org.devinebyte.compiler.api;

import java.util.List;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;

/**
 * Final public compilation result returned by the compiler.
 */
public record CompilationResult(

        boolean success,

        String message,

        List<Diagnostic> diagnostics,

        CompilerPipelineResult pipeline

) {

    public static CompilationResult success(
            String message,
            CompilerPipelineResult pipeline
    ) {

        return new CompilationResult(
                true,
                message,
                pipeline.diagnostics(),
                pipeline
        );
    }

    public static CompilationResult failure(
            String message,
            List<Diagnostic> diagnostics
    ) {

        return new CompilationResult(
                false,
                message,
                diagnostics,
                null
        );
    }

}
