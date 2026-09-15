package io.devinebyte.runtime.sdk.client;

import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.event.model.DomainEvent;
import java.util.function.Supplier;

public record WorkflowClient(
    Supplier<TenantContext> tenantSupplier,
    EventBus eventBus
) {
    
    /**
     * FIXED: Starts an isolated workflow state machine engine instance pipeline sequence.
     */
    public void start(DomainEvent workflowStartEvent) {
        TenantContext tenant = tenantSupplier.get();
        if (tenant == null) {
            throw new IllegalStateException("SDK Client Ingress Violation: Active tenant context could not be resolved out-of-band.");
        }
        if (workflowStartEvent == null) {
            throw new IllegalArgumentException("SDK Client Error: Dispatched workflow start event cannot be null.");
        }

        eventBus.publish(tenant, workflowStartEvent);
    }

    /**
     * FIXED: Signals an active, running automated state transition condition out-of-band.
     */
    public void signal(DomainEvent workflowSignalEvent) {
        TenantContext tenant = tenantSupplier.get();
        if (tenant == null) {
            throw new IllegalStateException("SDK Client Ingress Violation: Active tenant context could not be resolved out-of-band.");
        }
        if (workflowSignalEvent == null) {
            throw new IllegalArgumentException("SDK Client Error: Dispatched workflow signal event cannot be null.");
        }

        eventBus.publish(tenant, workflowSignalEvent);
    }
}

