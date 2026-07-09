package org.devinebyte.sdk.service;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

/**
 * Primary compilation service exposed by the SDK.
 */
public interface CompilationService {

    /**
     * Compiles a project inside a session.
     *
     * @param session compiler session with config/plugins
     * @param request compilation request: sources, options
     * @return compilation result
     */
    Result compile(Session session, Request request);
}
