package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilerConfiguration;
import org.devinebyte.compiler.core.CompilerEngine;
import org.devinebyte.compiler.core.CompilerPipelineResult;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;
import org.devinebyte.sdk.service.CompilationService;

public final class CompilerFacade implements CompilationService {

    @Override
    public Result compile(
            Session session,
            Request request
    ) {

        CompilerConfiguration configuration =
                ConfigurationMapper.map(
                        session,
                        request
                );

        CompilerEngine engine =
                new CompilerEngine(configuration);

        CompilerPipelineResult pipeline =
                engine.compile();

        return ResultMapper.map(pipeline);
    }
}
