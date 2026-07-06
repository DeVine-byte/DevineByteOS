package org.devinebyte.sdk.internal;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.Request;
import org.devinebyte.compiler.api.Diagnostic;
import org.devinebyte.compiler.api.DiagnosticSeverity;

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
