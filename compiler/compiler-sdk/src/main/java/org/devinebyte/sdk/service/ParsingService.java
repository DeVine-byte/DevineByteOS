package org.devinebyte.sdk.service;
import org.devinebyte.compiler.api.DiagnosticSeverity;

import org.devinebyte.compiler.api.Request;
import org.devinebyte.compiler.api.Result;

public interface ParsingService {

    Result parse(Request request);

}
