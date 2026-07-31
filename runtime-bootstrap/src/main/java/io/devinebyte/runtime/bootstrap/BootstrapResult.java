package io.devinebyte.runtime.bootstrap;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.nio.file.Path;

/**
 * Immutable result of bootstrap. If success=false, runtime must not proceed.
 */
public record BootstrapResult(
    boolean success,
    TenantContext tenantContext,
    ManifestReader.Manifest manifest,
    Path dbpkgPath,
    DiagnosticCollector diagnostics
) {}
