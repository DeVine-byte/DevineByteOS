package com.devinebyte.compiler.diagnostics;

public record Diagnostic(

        Severity severity,

        String code,

        String message

) {}
