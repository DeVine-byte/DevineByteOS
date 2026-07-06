package org.devinebyte.compiler.generator.engine;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

public record GenerationContext(

        Path outputDirectory,

        boolean overwriteExisting,

        boolean formatCode

) {}
