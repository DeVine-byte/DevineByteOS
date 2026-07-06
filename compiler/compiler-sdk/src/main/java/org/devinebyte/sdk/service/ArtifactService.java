package org.devinebyte.sdk.service;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;

public interface ArtifactService {

    Result generate(Request request, Path outputDirectory);

}
