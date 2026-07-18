package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;

import java.nio.file.Path;
import java.util.List;

/**
 * Captures the outcome of the complete compiler pipeline.
 *
 * This is an internal compiler object passed between
 * CompilerEngine and CompilerFacade.
 */
public record CompilerPipelineResult(

        boolean success,

        String message,

        int sourceFiles,

        int tokenCount,

        int declarationCount,

        int generatedFiles,

        List<Path> artifacts,

        List<Diagnostic> diagnostics

) {

    public static CompilerPipelineResult success(

            int sourceFiles,

            int tokenCount,

            int declarationCount,

            List<Path> artifacts,

            List<Diagnostic> diagnostics

    ) {

        return new CompilerPipelineResult(

                true,

                "Compilation completed successfully.",

                sourceFiles,

                tokenCount,

                declarationCount,

                artifacts == null ? 0 : artifacts.size(),

                artifacts == null ? List.of() : List.copyOf(artifacts),

                diagnostics == null ? List.of() : List.copyOf(diagnostics)
        );
    }

    public static CompilerPipelineResult failure(

            String message,

            List<Diagnostic> diagnostics

    ) {

        return new CompilerPipelineResult(

                false,

                message,

                0,

                0,

                0,

                0,

                List.of(),

                diagnostics == null ? List.of() : List.copyOf(diagnostics)
        );
    }

    public static CompilerPipelineResult empty() {

        return new CompilerPipelineResult(

                true,

                "No compilation performed.",

                0,

                0,

                0,

                0,

                List.of(),

                List.of()
        );
    }
}
