package org.devinebyte.compiler.generator.engine;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public class GeneratorException extends RuntimeException {

    public GeneratorException(String message) {
        super(message);
    }

}
