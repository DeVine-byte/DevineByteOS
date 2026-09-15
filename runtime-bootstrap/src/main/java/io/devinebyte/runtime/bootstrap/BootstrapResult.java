package io.devinebyte.runtime.bootstrap;

import io.devinebyte.compiler.dsl.generator.ApiSchemaWriter.ApiSchema;
import io.devinebyte.compiler.packaging.model.Manifest; // FIXED: Centralized model target
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.nio.file.Path;
import java.util.List;

/**
 * Immutable result of bootstrap. If success=false, runtime must not proceed.
 */
public record BootstrapResult(
    boolean success,
    TenantContext tenantContext, 
    Manifest manifest, // FIXED: Reference unified class entity directly
    Path dbpkgPath,
    DiagnosticCollector diagnostics,
    List<ApiSchema> apiSchemas 
) {}

