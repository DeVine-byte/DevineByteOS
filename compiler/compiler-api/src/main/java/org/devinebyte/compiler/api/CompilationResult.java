package org.devinebyte.compiler.api;

public record CompilationResult(
    boolean success,
    String output,
    String error
) {}
