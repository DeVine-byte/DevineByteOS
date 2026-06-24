package com.devinebyte.compiler.core;

public record CompilerConfiguration(

        String outputDirectory,

        boolean strictMode,

        boolean incremental

) {}
