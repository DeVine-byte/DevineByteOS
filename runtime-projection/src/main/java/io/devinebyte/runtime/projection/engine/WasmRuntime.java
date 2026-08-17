package io.devinebyte.runtime.projection.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.compiler.projection.model.ProjectionFunction;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.diagnostics.Diagnostic;
import io.devinebyte.runtime.core.diagnostics.DiagnosticSeverity;
import io.devinebyte.runtime.event.model.DomainEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class WasmRuntime implements AutoCloseable {
    private final ObjectMapper mapper;
    private final Map<String, java.util.function.Function<String, String>> registry = new ConcurrentHashMap<>();

    @Inject
    public WasmRuntime(ObjectMapper mapper) {
        this.mapper = mapper;
        registry.put("fold_sales_order_created", this::demoFold);
    }

    private String demoFold(String eventJson) {
        System.out.println("[PROJECTION-FAKE] Processing: " + eventJson);
        return "{\"state\":\"updated\"}";
    }

    public JsonNode invoke(TenantContext tenant, ProjectionContext ctx, ProjectionFunction fn, DomainEvent event) {
        try {
            byte[] wasmBytes = Base64.getDecoder().decode(fn.wasmBytecodeBase64());
            System.out.println("[PROJECTION][" + tenant.tenantId() + "] Loaded " + wasmBytes.length + " bytes for " + fn.name());

            var handler = registry.get(fn.name());
            if (handler == null) throw new IllegalStateException("No handler registered for: " + fn.name());

            String eventJson = mapper.writeValueAsString(event);
            String resultJson = handler.apply(eventJson);
            return mapper.readTree(resultJson);

        } catch (Exception e) {
            // FIX: 5 args
            ctx.diagnostics().add(new Diagnostic(
                "DBRT601", 
                DiagnosticSeverity.ERROR, 
                "Projection invoke failed: " + e.getMessage(),
                tenant.tenantId(),
                Instant.now()
            ));
            return null;
        }
    }

    @Override public void close() { }
}
