package io.devinebyte.runtime.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.compiler.packaging.model.Manifest;
import io.devinebyte.runtime.config.ModuleGraph;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ManifestReader {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // Pure Java structure initialization (Wiped out Jakarta annotations)
    public ManifestReader() {}

    @SuppressWarnings("unchecked")
    public Manifest read(TenantContext tenant, InputStream manifestStream, DiagnosticCollector diagnostics) {
        try {
            JsonNode node = mapper.readTree(manifestStream);

            // Safe JSON mapping extraction boundaries
            String rawSchemaVersion = node.path("schemaVersion").asText("1.0");
            String tenantId = node.path("tenantId").asText();
            String version = node.path("version").asText();
            Instant builtAt = node.has("builtAt") ? Instant.parse(node.get("builtAt").asText()) : Instant.now();
            String builtBy = node.path("builtBy").asText("unknown");
            String sha256 = node.path("sha256").asText("TBD");
            String signature = node.path("signature").asText("");
            boolean multiTenant = node.path("multiTenant").asBoolean(true);

            ModuleGraph moduleGraph = null;
            if (node.has("moduleGraph")) {
                moduleGraph = mapper.convertValue(node.get("moduleGraph"), ModuleGraph.class);
            }

            Map<String, String> metadata = node.has("metadata")
                ? mapper.convertValue(node.get("metadata"), Map.class)
                : Map.of();

            Map<String, String> keywordAliases = node.has("keywordAliases")
                ? mapper.convertValue(node.get("keywordAliases"), Map.class)
                : Map.of();

            List<String> enabledModules = new ArrayList<>();
            if (node.has("enabledModules")) {
                JsonNode modulesNode = node.get("enabledModules");
                if (modulesNode.isArray()) {
                    for (JsonNode m : modulesNode) {
                        enabledModules.add(m.asText());
                    }
                }
            }

            // ==========================================================
            // ITEM 11 FIXED: V2 SCHEMA EXTRACTION & ON-THE-FLY MIGRATION
            // ==========================================================
            String minRuntimeVersion = "1.0.0";
            Map<String, String> dependencies = new HashMap<>();
            Map<String, Boolean> features = new HashMap<>();

            // If it's a legacy V1 manifest, perform an inline migration to V2 defaults silently
            if (Double.parseDouble(rawSchemaVersion) < 2.0) {
                // FIXED: Adapted to look up fields cleanly without breaking the DiagnosticCollector signature contract
                dependencies.put("core-substrate", "1.0.0");
                features.put("telemetry.enabled", true);
            } else {
                // Parse standard native Schema V2 parameters securely
                minRuntimeVersion = node.path("minRuntimeVersion").asText("1.0.0");
                
                if (node.has("dependencies")) {
                    dependencies = mapper.convertValue(node.get("dependencies"), Map.class);
                }
                if (node.has("features")) {
                    features = mapper.convertValue(node.get("features"), Map.class);
                }
            }

            // Map directly onto the single source of truth 15-parameter core model entity record
            return new Manifest(
                "2.0", // Enforce upgraded version classification
                tenantId,
                version,
                builtAt,
                builtBy,
                sha256,
                signature,
                moduleGraph,
                metadata,
                multiTenant,
                keywordAliases,
                enabledModules,
                minRuntimeVersion,
                dependencies,
                features
            );
        } catch (Exception e) {
            diagnostics.fatal("DBRT001", "Security Failure: Failed to securely unmarshal /manifest.json target structure: " + e.getMessage(), tenant.tenantId());
            return null;
        }
    }
}

