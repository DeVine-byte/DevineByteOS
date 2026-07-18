package org.devinebyte.compiler.api;

import java.nio.file.Path;
import java.util.List;

import org.devinebyte.compiler.api.diagnostics.Diagnostic;

/**
 * Captures the outcome of every stage in the compiler pipeline.
 *
 * This object is primarily intended for:
 *
 * - SDK
 * - CLI
 * - IDE integration
 * - Testing
 * - Diagnostics
 */
public record CompilerPipelineResult(

        int sourceFiles,

        int tokenCount,

        int declarationCount,

        int generatedFiles,

        List<Path> artifacts,

        List<Diagnostic> diagnostics

) {

    public static CompilerPipelineResult empty() {

        return new CompilerPipelineResult(
                0,
                0,
                0,
                0,
                List.of(),
                List.of()
        );
    }

}
