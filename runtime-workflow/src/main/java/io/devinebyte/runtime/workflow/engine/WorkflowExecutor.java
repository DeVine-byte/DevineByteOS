package io.devinebyte.runtime.workflow.engine;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.EventMetadata;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.workflow.model.WorkflowDefinition;
import io.devinebyte.runtime.repository.RepositoryFactory;
import io.devinebyte.runtime.repository.EntityRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkflowExecutor {
    private final EventStore store;
    private final ModuleIsolationGuard guard;

    public WorkflowExecutor(EventStore store, ModuleIsolationGuard guard) {
        this.store = store;
        this.guard = guard;
    }

    public Object start(TenantContext ctx, WorkflowDefinition def, Map<String, Object> input) {
        guard.assertEnabled(ctx, def.moduleId(), "start " + def.name());

        String instanceId = UUID.randomUUID().toString();
        String currentState = def.initialState();
        Map<String, Object> runtimeContext = new HashMap<>(input);
        
        runtimeContext.put("workflowInstanceId", instanceId);
        runtimeContext.put("workflow", def.name());
        runtimeContext.put("moduleId", def.moduleId());

        while (!"END".equals(currentState) && currentState != null) {
            var stateDef = def.statesByName().get(currentState);
            if (stateDef == null || stateDef.isFinal()) {
                break;
            }

            String nextState = null;
            for (var transition : stateDef.transitions()) {
                String action = transition.action();

                try {
                    if ("builtin:repository:upsert".equals(action)) {
                        String tenantId = ctx.tenantId();
                        String moduleId = def.moduleId(); 
                        
                        String entityName = def.name()
                            .replace("Handle", "")
                            .replace("POST", "")
                            .replace("GET", "")
                            .replace("PUT", "");

                        EntityRepository repo = RepositoryFactory.get(tenantId, moduleId, entityName);
                        String persistentId = repo.upsert(runtimeContext);
                        
                        System.out.println("[EXECUTOR DEBUG] Implicit workflow wrote record with ID: " + persistentId);
                        
                        runtimeContext.put("id", persistentId);
                        nextState = transition.targetState();
                        break;
                    }
                } catch (Exception ex) {
                    System.err.println("[CORE RUNTIME CRASH] Step operation fault: " + action + " -> " + ex.getMessage());
                    runtimeContext.put("errorMessage", ex.getMessage());
                    
                    var errRoute = stateDef.transitions().stream()
                        .filter(t -> t.action() != null && t.action().startsWith("builtin:error"))
                        .findFirst();
                    
                    if (errRoute.isPresent()) {
                        currentState = errRoute.get().targetState();
                        nextState = null;
                        break;
                    } else {
                        throw new RuntimeException("Unhandled execution loop breakdown", ex);
                    }
                }
            }

            if (nextState != null) {
                currentState = nextState;
            } else {
                break;
            }
        }

        if ("FailExecution".equals(currentState)) {
            throw new RuntimeException("500: Internal Server Error - DB_UNAVAILABLE: " + runtimeContext.get("errorMessage"));
        }

        return runtimeContext;
    }

    public WorkflowInstance handleEvent(TenantContext ctx, WorkflowDefinition def, WorkflowInstance instance, DomainEvent event) {
        guard.assertEnabled(ctx, def.moduleId(), "handleEvent for " + def.name());
        var transition = def.findTransition(instance.currentState(), event.type());
        if (transition == null) return instance;

        String nextState = transition.targetState();
        boolean isFinal = "END".equals(nextState) || def.statesByName().get(nextState).isFinal();
        WorkflowInstance newInstance = instance.advance(nextState, isFinal);

        ObjectNode payload = (ObjectNode) event.payload();
        payload.put("workflowInstanceId", instance.instanceId().toString());
        payload.put("fromState", instance.currentState());
        payload.put("toState", nextState);
        payload.put("action", transition.action());

        EventMetadata meta = new EventMetadata(UUID.randomUUID(), null, "workflow", Instant.now(),
            java.util.Map.of("tenantId", ctx.tenantId(), "workflow", def.name()));
        DomainEvent stateEvent = new DomainEvent("WorkflowTransitioned", "1.0", payload, meta);
        store.append(ctx, stateEvent);
        return newInstance;
    }
}

