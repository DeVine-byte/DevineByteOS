package org.devinebyte.compiler.cli.sdk;

import java.nio.file.Path;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Session;

public final class SessionFactory {

    private final CompilerSDK sdk;

    public SessionFactory(CompilerSDK sdk) {
        this.sdk = sdk;
    }

    public Session create(CliOptions options) {

        Path projectRoot = options.getProjectRoot();
        Path sourceDirectory = options.getSourceDirectory();
        Path outputDirectory = options.getOutputDirectory();

        return sdk.builder()
                .projectRoot(projectRoot)
                .sourceDirectory(sourceDirectory)
                .outputDirectory(outputDirectory)
                .incremental(options.isIncremental())
                .optimize(options.isOptimize())
                .build();
    }
}
