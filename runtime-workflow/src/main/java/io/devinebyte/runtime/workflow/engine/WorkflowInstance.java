package io.devinebyte.runtime.workflow.engine;

import io.devinebyte.runtime.core.context.TenantContext;
import java.time.Instant;
import java.util.UUID;

public record WorkflowInstance(
    UUID instanceId,
    TenantContext tenant,
    String workflowName,
    String currentState,
    Instant startedAt,
    boolean completed
) {
    public WorkflowInstance advance(String nextState, boolean isFinal) {
        return new WorkflowInstance(instanceId, tenant, workflowName, nextState, startedAt, isFinal);
    }
}
