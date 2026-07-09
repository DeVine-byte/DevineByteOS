package org.devinebyte.sdk.service;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;

/**
 * Service responsible for lexical analysis and parsing.
 */
public interface ParsingService {

    /**
     * Parses the supplied request.
     *
     * @param request compilation request
     * @return parsing result
     */
    Result parse(Request request);

}
