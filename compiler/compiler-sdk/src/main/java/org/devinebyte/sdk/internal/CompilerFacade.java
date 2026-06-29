package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.core.CompilerConfiguration;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

public final class CompilerFacade {

    public Result compile(Session session, Request request) {

        CompilerConfiguration configuration =
                ConfigurationMapper.map(session);

        CompilerEngine engine = new CompilerEngine();

        engine.start();

        return Result.success();
    }

}
