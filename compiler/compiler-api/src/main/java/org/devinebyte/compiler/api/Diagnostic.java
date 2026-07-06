package org.devinebyte.compiler.api;
public record Diagnostic(DiagnosticSeverity severity, String code, String message, int line, int column) {}
