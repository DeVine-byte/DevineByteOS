package org.devinebyte.sdk.plugin;

import org.devinebyte.sdk.service.ArtifactService;
import org.devinebyte.sdk.service.CompilationService;
import org.devinebyte.sdk.service.ParsingService;
import org.devinebyte.sdk.service.ValidationService;

import java.util.Map;

/**
 * Context provided to compiler plugins.
 *
 * Exposes stable SDK services and configuration without exposing
 * compiler implementation details.
 */
public interface PluginContext {

    /**
     * Compiler configuration properties.
     */
    Map<String, String> properties();

    /**
     * Parsing service.
     */
    ParsingService parsingService();

    /**
     * Validation service.
     */
    ValidationService validationService();

    /**
     * Compilation service.
     */
    CompilationService compilationService();

    /**
     * Artifact generation service.
     */
    ArtifactService artifactService();

    /**
     * Registers an event listener.
     */
    void registerListener(EventListener listener);
}
