package io.devinebyte.runtime.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.DiagnosticCollector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.InputStream;
import java.time.Instant;

@Singleton
public class ManifestReader {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Inject
    public ManifestReader() {}

    public record Manifest(
        String schemaVersion,
        String tenantId,
        String version,
        Instant builtAt,
        String builtBy,
        @JsonProperty("sha256") String checksumSha256,
        String signature,
        @JsonProperty("multiTenant") boolean multiTenant // NEW
    ) {
        public String checksumSha256() { return checksumSha256; }
    }

    public Manifest read(TenantContext tenant, InputStream manifestStream, DiagnosticCollector diagnostics) {
        try {
            JsonNode node = mapper.readTree(manifestStream);
            return new Manifest(
                node.get("schemaVersion").asText(),
                node.get("tenantId").asText(),
                node.get("version").asText(),
                Instant.parse(node.get("builtAt").asText()),
                node.get("builtBy").asText(),
                node.get("sha256").asText(),
                node.get("signature").asText(),
                node.path("multiTenant").asBoolean(true) // default true for v1
            );
        } catch (Exception e) {
            diagnostics.fatal("DBRT001", "Failed to parse /manifest.json: " + e.getMessage(), tenant.tenantId());
            return null;
        }
    }
}
