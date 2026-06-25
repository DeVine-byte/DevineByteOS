package com.devinebyte.compiler.generator.engine;

import java.nio.file.Path;

public record GenerationContext(

        Path outputDirectory,

        boolean overwriteExisting,

        boolean formatCode

) {}
