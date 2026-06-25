package org.devinebyte.compiler.core;

public record CompilerConfiguration(

        String outputDirectory,

        boolean strictMode,

        boolean incremental

) {}
