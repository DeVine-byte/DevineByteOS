package io.devinebyte.compiler.projection.model;

public record ProjectionFunction(
    String name,
    String eventType,
    String wasmBytecodeBase64 // fold(event) -> update projection state
) {}
