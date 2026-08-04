package io.devinebyte.compiler.packaging.model;

import io.devinebyte.runtime.config.ModuleGraph;
import java.time.Instant;
import java.util.Map;

public record Manifest(
    String schemaVersion,
    String tenantId,
    String version,
    Instant builtAt,
    String builtBy,
    String sha256,
    String signature,
    ModuleGraph moduleGraph,
    Map<String, String> metadata,
    boolean multiTenant,
    Map<String, String> keywordAliases // ADD
) {
    public Manifest {
        keywordAliases = keywordAliases == null ? Map.of() : keywordAliases;
    }
}
