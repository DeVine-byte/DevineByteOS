package org.devinebyte.compiler.blueprint.loader;

import java.nio.file.Path;

public interface BlueprintLoader {

    BlueprintFile load(Path file);

}
