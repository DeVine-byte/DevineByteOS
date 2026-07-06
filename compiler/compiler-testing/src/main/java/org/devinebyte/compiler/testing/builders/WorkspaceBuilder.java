package org.devinebyte.compiler.testing.builders;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WorkspaceBuilder {

    public Path build() throws IOException {

        return Files.createTempDirectory(
                "compiler-workspace");

    }

}
