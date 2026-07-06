package org.devinebyte.compiler.cli.sdk;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.compiler.sdk.request.CompilerRequest;

public class RequestMapper {

    public CompilerRequest compileRequest(CliOptions options) {

        return CompilerRequest.builder()
                .input(options.getInput())
                .output(options.getOutput())
                .incremental(options.isIncremental())
                .strict(options.isStrict())
                .optimize(options.isOptimize())
                .verbose(options.isVerbose())
                .build();
    }
}
