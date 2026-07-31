package io.devinebyte.runtime.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Singleton
public final class ConfigurationManager {
    private final ObjectMapper mapper;

    @Inject 
    public ConfigurationManager(ObjectMapper mapper) { 
        this.mapper = mapper; 
    }

    // ===== LEGACY: Load from extracted files on disk =====
    public TenantConfig loadTenantConfig(TenantContext tenant, Path runtimeDir) {
        return read(tenant, runtimeDir, "tenant_config.json", TenantConfig.class, "DBRT003");
    }

    public FeatureFlags loadFeatureFlags(TenantContext tenant, Path runtimeDir) {
        Path p = runtimeDir.resolve("feature_flags.json");
        if(!Files.exists(p)) return new FeatureFlags(Map.of());
        return read(tenant, runtimeDir, "feature_flags.json", FeatureFlags.class, "DBRT004");
    }

    public ModuleGraph loadModuleGraph(TenantContext tenant, Path runtimeDir) {
        return read(tenant, runtimeDir, "module_graph.json", ModuleGraph.class, "DBRT005");
    }

    // ===== NEW: Build in-memory from ManifestDTO for multi-tenant =====
    public TenantConfig createTenantConfig(String tenantId, ManifestDTO manifest) {
        // TenantConfig(String tenantId, String version, String builtAt)
        return new TenantConfig(
            tenantId,
            manifest.version(),
            manifest.builtAt().toString()
        );
    }

    public FeatureFlags createFeatureFlags(ManifestDTO manifest) {
        return new FeatureFlags(Map.of());
    }

    public ModuleGraph createModuleGraph(ManifestDTO manifest, TenantContext ctx) {
        // ModuleGraph(Map<String, ModuleDefinition>)
        return new ModuleGraph(Map.of());
    }

    private <T> T read(TenantContext tenant, Path runtimeDir, String file, Class<T> type, String errCode) {
        Path p = runtimeDir.resolve(file);
        if(!Files.exists(p)) {
            throw new ConfigLoadException(errCode, "Missing " + file + " for " + tenant.tenantId());
        }
        try { 
            return mapper.readValue(Files.readString(p), type); 
        } catch(IOException e) { 
            throw new ConfigLoadException(errCode + "1", "Parse error " + file + ": " + e.getMessage()); 
        }
    }
}
