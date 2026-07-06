package org.devinebyte.compiler.api.internal;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.core.CompilerConfiguration;

import org.devinebyte.compiler.api.Request;
import org.devinebyte.compiler.api.Result;
import org.devinebyte.compiler.api.Session;

public final class CompilerFacade {

    public Result compile(Session session, Request request) {

        CompilerConfiguration configuration =
                ConfigurationMapper.map(session);

        CompilerEngine engine = new CompilerEngine();

        engine.start();

        return Result.success();
    }

}
