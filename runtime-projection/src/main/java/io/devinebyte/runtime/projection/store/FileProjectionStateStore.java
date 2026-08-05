package io.devinebyte.runtime.projection.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@jakarta.inject.Singleton
public final class FileProjectionStateStore implements ProjectionStateStore {
    private final ObjectMapper mapper = new ObjectMapper();
    private byte[] wasmBytes;

    @Override
    public void save(TenantContext tenant, String projectionName, JsonNode state) {
        try {
            Path base = Path.of("/data/tenants", tenant.tenantId(), "projections");
            Files.createDirectories(base);
            Path file = base.resolve(projectionName + ".json");
            mapper.writeValue(file.toFile(), state);
        } catch (Exception ignored) {}
    }

    @Override
    public Optional<JsonNode> load(TenantContext tenant, String projectionName) {
        try {
            Path file = Path.of("/data/tenants", tenant.tenantId(), "projections", projectionName + ".json");
            return Files.exists(file) ? Optional.of(mapper.readTree(file.toFile())) : Optional.empty();
        } catch (Exception e) { return Optional.empty(); }
    }

    @Override public byte[] loadWasmBytes() { return wasmBytes; }
    public void setWasmBytes(byte[] bytes) { this.wasmBytes = bytes; }
}
