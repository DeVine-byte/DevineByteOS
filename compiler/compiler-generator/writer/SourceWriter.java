package com.devinebyte.compiler.generator.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceWriter {

    public void write(Path file, String content) throws IOException {

        Files.createDirectories(file.getParent());

        Files.writeString(file, content);

    }

}
