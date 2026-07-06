package org.devinebyte.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

public final class CompilerSDK {

    private CompilerSDK() {
    }

    public static Builder builder() {
        return new Builder();
    }

}
