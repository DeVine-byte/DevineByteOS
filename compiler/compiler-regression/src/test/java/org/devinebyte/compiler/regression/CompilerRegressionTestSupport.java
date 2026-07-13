package org.devinebyte.compiler.regression;

import java.nio.file.Path;

import org.devinebyte.sdk.CompilerSDK;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

public abstract class CompilerRegressionTestSupport {

    protected Result compile(Path project, Path output) {

        Session session = CompilerSDK.builder()
                .projectRoot(project)
                .sourceDirectory(project)
                .outputDirectory(output)
                .incremental(false)
                .optimize(false)
                .build();

        Request request = session.request(project);

        return session.compile(request);
    }
}
