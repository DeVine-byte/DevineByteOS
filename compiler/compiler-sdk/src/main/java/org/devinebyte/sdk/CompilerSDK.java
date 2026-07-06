package org.devinebyte.compiler.api;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

public final class CompilerSDK {

    private CompilerSDK() {
    }

    public static Builder builder() {
        return new Builder();
    }

}
