package io.devinebyte.runtime.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.devinebyte.runtime.core.context.TenantContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
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

    // ===== NEW: Build in-memory from ManifestDTO + dbpkg =====
    public TenantConfig createTenantConfig(String tenantId, ManifestDTO manifest) {
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
        // We need the dbpkg path. It's not passed here. 
        // So we overload this and call the other one from TenantRuntimeFactory
        throw new UnsupportedOperationException("Use createModuleGraphFromDbpkg(Path) instead");
    }

    public ModuleGraph createModuleGraphFromDbpkg(Path dbpkg) throws Exception {
        try (FileSystem fs = FileSystems.newFileSystem(dbpkg)) {
            Path graphPath = fs.getPath("runtime/module_graph.json");
            if (!Files.exists(graphPath)) {
                return new ModuleGraph(Map.of());
            }

            try (InputStream in = Files.newInputStream(graphPath)) {
                JsonNode root = mapper.readTree(in);
                JsonNode modulesNode = root.get("modules");
                
                Map<String, ModuleGraph.ModuleDefinition> modules = new HashMap<>();

                if (modulesNode != null) {
                    modulesNode.fields().forEachRemaining(entry -> {
                        String moduleId = entry.getKey();
                        JsonNode def = entry.getValue();

                        boolean enabled = def.path("enabled").asBoolean(false);

                        Set<String> dependsOn = new HashSet<>();
                        if (def.has("dependsOn")) {
                            def.get("dependsOn").forEach(d -> dependsOn.add(d.asText()));
                        }

                        Set<String> exposes = new HashSet<>();
                        if (def.has("exposesEvents")) {
                            def.get("exposesEvents").forEach(e -> exposes.add(e.asText()));
                        }

                        Set<String> subscribes = new HashSet<>();
                        if (def.has("subscribesToEvents")) {
                            def.get("subscribesToEvents").forEach(s -> subscribes.add(s.asText()));
                        }

                        modules.put(moduleId, new ModuleGraph.ModuleDefinition(
                            moduleId, enabled, dependsOn, exposes, subscribes
                        ));
                    });
                }

                return new ModuleGraph(modules);
            }
        }
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
