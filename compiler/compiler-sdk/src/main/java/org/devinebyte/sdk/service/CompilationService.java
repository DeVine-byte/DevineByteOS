package org.devinebyte.sdk.service;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;

/**
 * Primary compilation service exposed by the SDK.
 *
 * Implementations are responsible for executing the
 * complete compiler pipeline.
 */
public interface CompilationService {

    /**
     * Compiles a project.
     *
     * @param request compiler request
     * @return compilation result
     */
    Result compile(Request request);
}
