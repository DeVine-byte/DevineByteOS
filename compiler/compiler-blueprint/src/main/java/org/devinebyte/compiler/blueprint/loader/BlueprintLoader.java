package org.devinebyte.compiler.blueprint.loader;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

public interface BlueprintLoader {

    BlueprintFile load(Path file);

}
