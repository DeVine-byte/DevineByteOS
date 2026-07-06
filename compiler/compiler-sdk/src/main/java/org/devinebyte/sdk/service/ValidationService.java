package org.devinebyte.compiler.api.service;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.api.Request;
import org.devinebyte.compiler.api.Result;

public interface ValidationService {

    Result validate(Request request);

}
