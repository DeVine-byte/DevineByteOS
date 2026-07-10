package org.devinebyte.compiler.e2e;

import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base support class for end-to-end compiler tests.
 *
 * <p>Creates an isolated compiler session and compiles
 * complete projects using the public SDK.</p>
 */
public abstract class CompilerE2ETestSupport {

    protected Result compile(Path sourceFile) {
        return doCompile(sourceFile, false);
    }

    protected Result compileIncremental(Path sourceFile) {
        return doCompile(sourceFile, true);
    }

    private Result doCompile(Path sourceFile, boolean incremental) {
        try {
            Path projectRoot = Files.createTempDirectory("dbos-e2e");
            Path outputDirectory = Files.createDirectories(projectRoot.resolve("build"));

            Session session = CompilerSDK.builder()
                    .projectRoot(projectRoot)
                    .sourceDirectory(sourceFile.getParent())
                    .outputDirectory(outputDirectory)
                    .incremental(incremental)
                    .optimize(true)
                    .build();

            Request request = session.request(sourceFile);
            return session.compile(request);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to execute compilation.", ex);
        }
    }
}
