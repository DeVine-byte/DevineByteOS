package org.devinebyte.compiler.core;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public record CompilerConfiguration(

        String outputDirectory,

        boolean strictMode,

        boolean incremental

) {}
