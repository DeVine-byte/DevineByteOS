package io.devinebyte.compiler.audit.parser;

import io.devinebyte.compiler.audit.model.AuditModel;

public record AuditParseResult(
    AuditModel model,
    boolean hasErrors
) {}
