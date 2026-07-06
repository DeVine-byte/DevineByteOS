package org.devinebyte.compiler.cli.console;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public interface Console {

    void info(String message);

    void success(String message);

    void warning(String message);

    void error(String message);

    void print(String message);
}
