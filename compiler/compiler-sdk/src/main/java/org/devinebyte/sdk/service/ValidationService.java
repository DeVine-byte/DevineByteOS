package org.devinebyte.sdk.service;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;

/**
 * Service responsible for validating a DevineByte project
 * without generating runtime artifacts.
 */
public interface ValidationService {

    /**
     * Validates the supplied project.
     *
     * @param request compiler request
     * @return validation result
     */
    Result validate(Request request);
}
