package org.devinebyte.sdk;

import java.nio.file.Path;

public final class Request {

    private final Path sourceFile;

    public Request(Path sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Path getSourceFile() {
        return sourceFile;
    }

}
