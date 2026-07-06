package org.devinebyte.compiler.blueprint.loader;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

public interface BlueprintLoader {

    BlueprintFile load(Path file);

}
