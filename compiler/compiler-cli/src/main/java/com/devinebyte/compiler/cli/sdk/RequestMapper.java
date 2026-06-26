package com.devinebyte.compiler.cli.sdk;

import com.devinebyte.compiler.cli.options.CliOptions;
import com.devinebyte.compiler.sdk.request.CompilerRequest;

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
