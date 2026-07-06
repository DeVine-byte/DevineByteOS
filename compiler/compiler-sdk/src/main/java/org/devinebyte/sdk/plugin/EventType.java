package org.devinebyte.sdk.plugin;
import org.devinebyte.compiler.api.DiagnosticSeverity;

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
