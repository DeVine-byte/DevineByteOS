package io.devinebyte.compiler.contracts.model;

import java.util.Map;

public record EventSchema(
    String name,
    String version,
    Map<String, FieldType> fields
) {}
