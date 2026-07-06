package org.devinebyte.sdk.service;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;

public interface ParsingService {

    Result parse(Request request);

}
