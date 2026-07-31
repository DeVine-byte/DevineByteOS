package io.devinebyte.compiler.audit.model;

import java.util.List;

public record BusinessUnit(
    String id,
    String name,
    List<String> modules
) {}
