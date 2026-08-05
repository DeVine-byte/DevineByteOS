package io.devinebyte.runtime.projection.store;

import com.fasterxml.jackson.databind.JsonNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.core.workflow.WorkflowInstanceReader;
import io.devinebyte.runtime.core.workflow.WorkflowInstanceView;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.workflow.engine.WorkflowInstance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.*;

@Singleton
public final class WorkflowInstanceStore implements WorkflowInstanceReader {
    private final EventStore store;

    @Inject
    public WorkflowInstanceStore(EventStore store) { this.store = store; }

    // Rule 6: Deterministic Replay. Fold all events
    @Override
    public List<WorkflowInstanceView> findActiveByTenant(TenantContext tenant) {
        Map<UUID, WorkflowInstance> instances = new HashMap<>();
        for (DomainEvent event : store.readAll(tenant)) {
            replay(event, instances);
        }
        return instances.values().stream()
               .filter(i ->!i.completed())
               .map(i -> new WorkflowInstanceView(
                    i.instanceId(),
                    i.tenant().tenantId(),
                    i.workflowName(),
                    i.currentState(),
                    i.completed()
                ))
               .toList();
    }

    private void replay(DomainEvent event, Map<UUID, WorkflowInstance> instances) {
        String type = event.type();
        JsonNode payload = event.payload();
        UUID instanceId = event.metadata().correlationId();
        if (instanceId == null) return;

        switch (type) {
            case "WorkflowStarted" -> {
                String workflowName = payload.get("workflowName").asText();
                String initialState = payload.get("initialState").asText();
                instances.put(instanceId, new WorkflowInstance(
                    instanceId,
                    TenantContext.of(event.tenantId()),
                    workflowName,
                    initialState,
                    event.occurredAt(),
                    false
                ));
            }
            case "WorkflowStateAdvanced" -> {
                WorkflowInstance current = instances.get(instanceId);
                if (current!= null) {
                    String nextState = payload.get("nextState").asText();
                    boolean isFinal = payload.get("isFinal").asBoolean();
                    instances.put(instanceId, current.advance(nextState, isFinal));
                }
            }
            case "WorkflowCompleted" -> {
                WorkflowInstance current = instances.get(instanceId);
                if (current!= null) {
                    instances.put(instanceId, current.advance(current.currentState(), true));
                }
            }
        }
    }
}
