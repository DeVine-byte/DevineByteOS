package org.devinebyte.sdk;

import org.devinebyte.sdk.diagnostics.Diagnostic;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Provides contextual information available during compilation.
 *
 * <p>The compiler context exposes the working directory,
 * compiler options, and collected diagnostics for a
 * compilation request.</p>
 */
public interface CompilerContext {

    /**
     * Returns the compiler working directory.
     *
     * @return working directory
     */
    File workingDirectory();

    /**
     * Returns compiler options.
     *
     * @return immutable option map
     */
    Map<String, String> options();

    /**
     * Returns diagnostics collected during compilation.
     *
     * @return diagnostics
     */
    List<Diagnostic> diagnostics();
}
