package org.devinebyte.compiler.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TemporaryWorkspace {

    private TemporaryWorkspace() {
    }

    public static Path create() throws IOException {

        return Files.createTempDirectory("devinebyte-test");

    }

}
