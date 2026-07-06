package org.devinebyte.compiler.cli.sdk; 
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.CompilerSDK; 
import org.devinebyte.compiler.api.session.BuildSession;

import java.util.Objects;

/**
 * Creates BuildSession instances for CLI commands.
 *
 * This class isolates the CLI from the SDK implementation and provides
 * a single location for session creation.
 */
public final class SessionFactory {

    private final CompilerSDK compilerSDK;

    public SessionFactory(CompilerSDK compilerSDK) {
        this.compilerSDK = Objects.requireNonNull(
                compilerSDK,
                "compilerSDK must not be null"
        );
    }

    /**
     * Creates a new compiler build session.
     *
     * @return a new BuildSession
     */
    public BuildSession createSession() {
        return compilerSDK.createSession();
    }
}
