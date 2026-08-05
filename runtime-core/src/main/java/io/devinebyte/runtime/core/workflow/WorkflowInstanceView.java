package io.devinebyte.runtime.core.workflow;

import java.util.UUID;

public record WorkflowInstanceView(
    UUID instanceId,
    String tenantId,
    String workflowName,
    String currentState,
    boolean completed
) {}
