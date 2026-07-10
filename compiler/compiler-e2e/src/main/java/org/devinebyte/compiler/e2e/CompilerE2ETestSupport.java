package org.devinebyte.compiler.e2e;

import org.devinebyte.sdk.Builder;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Base support class for end-to-end compiler tests.
 *
 * <p>Creates an isolated compiler session and compiles
 * complete projects using the public SDK.</p>
 */
public abstract class CompilerE2ETestSupport {

    /**
     * Performs a full compilation.
     *
     * @param sourceFile source file to compile
     * @return compilation result
     */
    protected Result compile(Path sourceFile) {

        try {
            Path projectRoot = Files.createTempDirectory("dbos-e2e");
            Path outputDirectory = Files.createDirectories(
                    projectRoot.resolve("build"));

            CompilerSDK sdk = CompilerSDK.builder().build();

            Session session = sdk.newSession()
                    .projectRoot(projectRoot)
                    .sourceDirectory(sourceFile.getParent())
                    .outputDirectory(outputDirectory)
                    .incremental(false)
                    .optimize(true)
                    .context(defaultContext(projectRoot))
                    .build();

            Request request = session.request(
                    sourceFile,
                    session.getContext());

            return session.compile(request);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to execute end-to-end compilation.", ex);
        }
    }

    /**
     * Performs an incremental compilation.
     *
     * @param sourceFile source file
     * @return compilation result
     */
    protected Result compileIncremental(Path sourceFile) {

        try {
            Path projectRoot = Files.createTempDirectory("dbos-e2e");
            Path outputDirectory = Files.createDirectories(
                    projectRoot.resolve("build"));

            CompilerSDK sdk = CompilerSDK.builder().build();

            Session session = sdk.newSession()
                    .projectRoot(projectRoot)
                    .sourceDirectory(sourceFile.getParent())
                    .outputDirectory(outputDirectory)
                    .incremental(true)
                    .optimize(true)
                    .context(defaultContext(projectRoot))
                    .build();

            Request request = session.request(
                    sourceFile,
                    session.getContext());

            return session.compile(request);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to execute incremental compilation.", ex);
        }
    }

    /**
     * Creates the default compiler context.
     */
    protected CompilerContext defaultContext(Path workingDirectory) {

        return new Builder.DefaultCompilerContext(
                workingDirectory.toFile(),
                Map.of()
        );
    }
}
