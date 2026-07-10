package org.devinebyte.compiler.testing.runners;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

/**
 * Executes compiler requests during tests.
 */
public final class CompilerRunner {

    /**
     * Executes a compilation request.
     *
     * @param session compilation session
     * @param request compilation request
     * @return compilation result
     */
    public Result run(Session session, Request request) {
        return session.compile(request);
    }
}
