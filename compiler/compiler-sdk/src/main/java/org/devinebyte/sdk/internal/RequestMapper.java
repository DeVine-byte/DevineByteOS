package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.cli.options.CliOptions; // CLI -> SDK boundary
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.CompilerRequest;
import org.devinebyte.sdk.diagnostics.DiagnosticCollector;

import java.util.Map;

public final class RequestMapper {

    public CompilerRequest compileRequest(CliOptions options) {
        CompilerContext ctx = new DefaultCompilerContext(
            options.outputDirectory(), 
            Map.of(), 
            new DiagnosticCollector()
        );

        return new CompilerRequest(
            options.inputFile(),
            options.outputDirectory(),
            ctx,
            options.incremental()
        );
    }
}
