package io.devinebyte.runtime.sdk.client;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.event.model.DomainEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public record EntityClient(
    Supplier<TenantContext> tenantSupplier,
    EventBus eventBus
) {
    
    /**
     * Hydrates the snapshot state configuration layout parameters for a targeted entity.
     */
    public Map<String, Object> load(String entityType, UUID entityId) {
        TenantContext tenant = tenantSupplier.get();
        String tenantId = tenant != null ? tenant.tenantId() : "UNKNOWN";
        
        Map<String, Object> mockSnapshot = new HashMap<>();
        mockSnapshot.put("id", entityId.toString());
        mockSnapshot.put("type", entityType);
        mockSnapshot.put("tenantId", tenantId);
        mockSnapshot.put("status", "HYDRATED");
        mockSnapshot.put("version", 1L);
        
        return Map.copyOf(mockSnapshot);
    }

    /**
     * FIXED: Publishes an entity state mutation event into the platform communication event stream.
     */
    public void save(DomainEvent genericMutationEvent) {
        TenantContext tenant = tenantSupplier.get();
        if (tenant == null) {
            throw new IllegalStateException("SDK Client Ingress Violation: Active tenant context could not be resolved out-of-band.");
        }
        if (genericMutationEvent == null) {
            throw new IllegalArgumentException("SDK Client Error: Dispatched domain mutation event cannot be null.");
        }

        // Forward the pre-formed transaction event directly through the framework channel
        eventBus.publish(tenant, genericMutationEvent);
    }
}

