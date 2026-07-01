package org.devinebyte.sdk;

import org.devinebyte.sdk.diagnostics.DiagnosticCollector;
import java.nio.file.Path;
import java.util.Map;

public interface CompilerContext {
    Path workingDirectory();
    Map<String, String> options();
    DiagnosticCollector diagnostics();
}
