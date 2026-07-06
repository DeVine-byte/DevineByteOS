package org.devinebyte.compiler.core;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public record CompilerConfiguration(

        String outputDirectory,

        boolean strictMode,

        boolean incremental

) {}
