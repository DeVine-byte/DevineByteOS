package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.diagnostics.Diagnostic;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RequestMapper {

    public Request compileRequest(CliOptions options) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        CompilerContext ctx = new CompilerContext() {
            @Override public java.io.File workingDirectory() { return options.getOutput().toFile(); }
            @Override public Map<String, String> options() { return Map.of(); }
            @Override public List<Diagnostic> diagnostics() { return diagnostics; }
        };

        return new Request(
                options.getInput().toFile(),
                options.getOutput().toFile(),
                ctx,
                options.isIncremental()
        );
    }
}
