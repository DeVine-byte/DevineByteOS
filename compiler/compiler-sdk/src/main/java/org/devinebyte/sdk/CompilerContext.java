package org.devinebyte.sdk;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import org.devinebyte.compiler.api.Diagnostic;
import java.io.File;
import java.util.List;
import java.util.Map;

public interface CompilerContext {
    File workingDirectory();
    Map<String, String> options();
    List<Diagnostic> diagnostics(); // was DiagnosticCollector
}
