package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.sdk.Session;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.compiler.cli.options.CliOptions;

import java.nio.file.Path;

public final class SessionFactory {

    public Session create(CliOptions options) {
        Path input = options.getInput();
        Path output = options.getOutput();
        
        Path projectRoot = input.getParent();
        Path sourceDirectory = input;
        Path outputDirectory = output;

        return CompilerSDK.builder()
                .projectRoot(projectRoot)
                .sourceDirectory(sourceDirectory)
                .outputDirectory(outputDirectory)
                .incremental(options.isIncremental())
                .optimize(options.isOptimize())
                .strict(options.isStrict())
                .verbose(options.isVerbose())
                .build();
    }
}
