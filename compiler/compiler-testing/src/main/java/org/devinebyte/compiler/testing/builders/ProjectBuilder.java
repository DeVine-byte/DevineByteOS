package org.devinebyte.compiler.testing.builders;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

public final class ProjectBuilder {

    private Path root;

    public ProjectBuilder root(Path root) {
        this.root = root;
        return this;
    }

    public Path build() {
        return root;
    }

}
