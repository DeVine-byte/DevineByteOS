package org.devinebyte.compiler.api.diagnostics;
public record Diagnostic(DiagnosticSeverity severity, String code, String message, int line, int column) {}
