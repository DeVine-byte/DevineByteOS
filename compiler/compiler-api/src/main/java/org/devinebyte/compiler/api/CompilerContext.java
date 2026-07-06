package org.devinebyte.compiler.api;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface CompilerContext {
    File workingDirectory();
    Map<String, String> options();
    List<Diagnostic> diagnostics();
}
