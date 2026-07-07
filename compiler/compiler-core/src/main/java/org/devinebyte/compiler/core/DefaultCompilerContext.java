package org.devinebyte.compiler.core;

import org.devinebyte.compiler.api.CompilerContext;
import org.devinebyte.compiler.api.diagnostics.Diagnostic;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record DefaultCompilerContext(
        File workingDirectory,
        Map<String, String> options,
        List<Diagnostic> diagnostics
) implements CompilerContext {

    public DefaultCompilerContext {
        options = options == null ? Collections.emptyMap() : Map.copyOf(options);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }
}
