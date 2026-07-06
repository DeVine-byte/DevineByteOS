package org.devinebyte.sdk.service;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import org.devinebyte.compiler.api.Request;
import org.devinebyte.compiler.api.Result;

public interface CompilationService {

    Result compile(Request request);

}
