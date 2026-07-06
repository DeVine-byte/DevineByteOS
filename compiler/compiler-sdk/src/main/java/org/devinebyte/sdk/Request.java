package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class Request {
    private final File sourceFile;
    private final File outputDirectory;
    private final CompilerContext context;
    private final boolean incremental;

    public Request(File sourceFile, File outputDirectory, CompilerContext context, boolean incremental) {
        this.sourceFile = sourceFile;
        this.outputDirectory = outputDirectory;
        this.context = context;
        this.incremental = incremental;
    }

    public File getSourceFile() { return sourceFile; }
    public File getOutputDirectory() { return outputDirectory; }
    public CompilerContext getContext() { return context; }
    public boolean isIncremental() { return incremental; }
}
