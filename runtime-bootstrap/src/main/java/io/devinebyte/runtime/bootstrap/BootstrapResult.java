package io.devinebyte.runtime.bootstrap;

import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema; // NEW
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.nio.file.Path;
import java.util.List; // NEW

/**
 * Immutable result of bootstrap. If success=false, runtime must not proceed.
 */
public record BootstrapResult(
    boolean success,
    TenantContext tenantContext, // keep your old name
    ManifestReader.Manifest manifest, // nested class
    Path dbpkgPath,
    DiagnosticCollector diagnostics,
    List<ApiSchema> apiSchemas // NEW: this was missing
) {}
