package org.devinebyte.compiler.api.plugin;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public enum EventType {

    COMPILATION_STARTED,

    PARSING_STARTED,
    PARSING_COMPLETED,

    VALIDATION_STARTED,
    VALIDATION_COMPLETED,

    COMPILATION_COMPLETED,

    ARTIFACT_GENERATED,

    ERROR_OCCURRED

}
