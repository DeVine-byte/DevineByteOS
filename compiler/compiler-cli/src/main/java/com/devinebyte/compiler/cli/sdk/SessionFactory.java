package com.devinebyte.compiler.cli.sdk;

import com.devinebyte.compiler.sdk.CompilerSDK;
import com.devinebyte.compiler.sdk.session.BuildSession;

public final class SessionFactory {

    private final CompilerSDK sdk;

    public SessionFactory(CompilerSDK sdk) {
        this.sdk = sdk;
    }

    public BuildSession create() {
        return sdk.createSession();
    }
}
