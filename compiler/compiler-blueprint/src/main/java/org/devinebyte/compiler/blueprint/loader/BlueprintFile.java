package org.devinebyte.compiler.blueprint.loader;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public record BlueprintFile(

        String fileName,

        String content

) {}
