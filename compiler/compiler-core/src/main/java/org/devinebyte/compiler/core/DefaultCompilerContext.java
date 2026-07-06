package org.devinebyte.compiler.core;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.diagnostics.DiagnosticCollector;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public record DefaultCompilerContext(
    Path workingDirectory,
    Map<String, String> options,
    DiagnosticCollector diagnostics
) implements CompilerContext {

    public DefaultCompilerContext {
        options = options == null ? Collections.emptyMap() : Map.copyOf(options);
    }
}
