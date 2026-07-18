package org.devinebyte.compiler.api;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;

import java.util.List;

/**
 * Public compilation result.
 *
 * This class is part of the compiler API and must not depend
 * on compiler-core implementation classes.
 */
public record CompilationResult(

        boolean success,

        String message,

        List<Diagnostic> diagnostics

) {

    public static CompilationResult success(
            String message,
            List<Diagnostic> diagnostics
    ) {

        return new CompilationResult(
                true,
                message,
                diagnostics == null ? List.of() : List.copyOf(diagnostics)
        );
    }

    public static CompilationResult failure(
            String message,
            List<Diagnostic> diagnostics
    ) {

        return new CompilationResult(
                false,
                message,
                diagnostics == null ? List.of() : List.copyOf(diagnostics)
        );
    }
}
