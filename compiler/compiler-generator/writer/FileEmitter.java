package com.devinebyte.compiler.generator.writer;

import java.nio.file.Path;

public class FileEmitter {

    private final SourceWriter writer = new SourceWriter();

    public void emit(Path file, String content) throws Exception {

        writer.write(file, content);

    }

}
