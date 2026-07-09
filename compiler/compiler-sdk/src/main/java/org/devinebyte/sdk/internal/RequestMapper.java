package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.diagnostics.Diagnostic;
import org.devinebyte.sdk.Request;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Maps SDK Request -> CompilerContext.
 */
public final class RequestMapper {

    private RequestMapper() {
    }

    public static CompilerContext map(Request request) {

        return new CompilerContext() {

            @Override
            public File workingDirectory() {
                return request.getOutputDirectory().toFile();
            }

            @Override
            public Map<String, String> options() {
                return Map.of(
                        "incremental",
                        Boolean.toString(request.isIncremental())
                );
            }

            @Override
            public List<Diagnostic> diagnostics() {
                return List.of();
            }
        };
    }
}
