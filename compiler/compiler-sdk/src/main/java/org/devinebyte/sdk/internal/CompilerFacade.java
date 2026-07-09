
package org.devinebyte.sdk.internal;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;
import org.devinebyte.sdk.service.CompilationService;

public final class CompilerFacade implements CompilationService {

    @Override
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

        throw new UnsupportedOperationException("Compiler pipeline has not yet been connected.");
    }
}
