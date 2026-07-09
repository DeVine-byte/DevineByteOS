package org.devinebyte.sdk.service;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Result;

import java.nio.file.Path;

/**
 * Service responsible for writing generated compiler artifacts
 * to the configured output directory.
 *
 * This abstraction allows alternative artifact storage
 * implementations (filesystem, cloud storage, etc.).
 */
public interface ArtifactService {

    /**
     * Writes generated artifacts.
     *
     * @param request compiler request
     * @param outputDirectory destination directory
     * @return operation result
     */
    Result generate(Request request, Path outputDirectory);
}
