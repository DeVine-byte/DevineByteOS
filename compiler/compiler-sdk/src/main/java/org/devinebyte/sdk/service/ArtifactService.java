package org.devinebyte.compiler.api.service;
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

import org.devinebyte.compiler.api.Request;
import org.devinebyte.compiler.api.Result;

public interface ArtifactService {

    Result generate(Request request, Path outputDirectory);

}
