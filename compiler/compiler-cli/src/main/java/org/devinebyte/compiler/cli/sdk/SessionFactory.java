package org.devinebyte.compiler.cli.sdk;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.sdk.CompilerSDK;
import org.devinebyte.compiler.sdk.session.BuildSession;

public final class SessionFactory {

    private final CompilerSDK sdk;

    public SessionFactory(CompilerSDK sdk) {
        this.sdk = sdk;
    }

    public BuildSession create() {
        return sdk.createSession();
    }
}
