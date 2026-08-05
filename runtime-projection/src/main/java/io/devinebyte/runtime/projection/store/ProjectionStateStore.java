package io.devinebyte.runtime.projection.store;

import com.fasterxml.jackson.databind.JsonNode;
import io.devinebyte.runtime.core.context.TenantContext;
import java.util.Optional;

public interface ProjectionStateStore {
    void save(TenantContext tenant, String projectionName, JsonNode state);
    Optional<JsonNode> load(TenantContext tenant, String projectionName);
    byte[] loadWasmBytes(); // set by loader
}
