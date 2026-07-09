package org.devinebyte.sdk.service;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;
import org.devinebyte.sdk.Session;

/**
 * Executes compiler requests.
 */
public interface CompilationService {

    Result compile(
            Session session,
            Request request
    );
}
