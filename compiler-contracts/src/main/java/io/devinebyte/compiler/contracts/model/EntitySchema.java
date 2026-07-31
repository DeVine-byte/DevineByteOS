package io.devinebyte.compiler.contracts.model;

import java.util.List;

public record EntitySchema(
    String name,
    String key,
    List<Field> fields
) {}
