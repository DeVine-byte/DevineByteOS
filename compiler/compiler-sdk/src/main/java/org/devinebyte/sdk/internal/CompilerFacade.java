package org.devinebyte.sdk.internal;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

/**
 * Internal bridge between the SDK and the compiler.
 *
 * This class is intentionally isolated from the public SDK API.
 * Its implementation will evolve as compiler-core becomes complete.
 */
public final class CompilerFacade {

    public Result compile(Session session, Request request) {

        // Future pipeline:
        //
        // Session
        //      ↓
        // ConfigurationMapper
        //      ↓
        // CompilerConfiguration
        //      ↓
        // CompilerEngine
        //      ↓
        // CompilationResult
        //      ↓
        // ResultMapper
        //      ↓
        // SDK Result

        throw new UnsupportedOperationException(
                "Compiler pipeline has not yet been connected.");
    }
}
