package org.devinebyte.compiler.blueprint.loader;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public record BlueprintFile(

        String fileName,

        String content

) {}
