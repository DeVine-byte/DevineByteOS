package org.devinebyte.sdk;
import org.devinebyte.compiler.api.DiagnosticSeverity;

public final class CompilerSDK {

    private CompilerSDK() {
    }

    public static Builder builder() {
        return new Builder();
    }

}
