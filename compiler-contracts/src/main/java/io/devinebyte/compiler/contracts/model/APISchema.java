package io.devinebyte.compiler.contracts.model;

public record APISchema(
    String path,
    String method,
    String commandOrQuery
) {}
