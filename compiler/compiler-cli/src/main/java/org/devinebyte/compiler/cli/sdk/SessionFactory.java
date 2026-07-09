package org.devinebyte.compiler.cli.sdk;

import java.nio.file.Path;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Session;

public final class SessionFactory {

    /**
     * Creates a compiler session from CLI options.
     */
    public Session create(CliOptions options) {

        Path projectRoot = options.getInput().toAbsolutePath().getParent();
        if (projectRoot == null) {
            projectRoot = Path.of(".");
        }

        Path sourceDirectory = options.getInput();
        Path outputDirectory = options.getOutput();

        return CompilerSDK.builder()
                .projectRoot(projectRoot)
                .sourceDirectory(sourceDirectory)
                .outputDirectory(outputDirectory)
                .incremental(options.isIncremental())
                .optimize(options.isOptimize())
                .build();
    }
}
