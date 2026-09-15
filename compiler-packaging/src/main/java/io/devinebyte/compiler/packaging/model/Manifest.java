package io.devinebyte.compiler.packaging.model;                  

import io.devinebyte.runtime.config.ModuleGraph;
import java.time.Instant;
import java.util.List;
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
    Map<String, String> keywordAliases,
    List<String> enabledModules,
    
    // ITEM 11 FIXED: Production V2 Schema Specification Extensions
    String minRuntimeVersion,
    Map<String, String> dependencies,
    Map<String, Boolean> features
) {
    public Manifest {
        keywordAliases = keywordAliases == null ? Map.of() : keywordAliases;
        enabledModules = enabledModules == null ? List.of() : enabledModules;
        dependencies = dependencies == null ? Map.of() : dependencies;
        features = features == null ? Map.of() : features;
        minRuntimeVersion = minRuntimeVersion == null ? "1.0.0" : minRuntimeVersion;
    }
}

