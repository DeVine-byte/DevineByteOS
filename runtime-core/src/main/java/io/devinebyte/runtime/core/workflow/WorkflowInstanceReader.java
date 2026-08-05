package io.devinebyte.runtime.core.workflow;

import io.devinebyte.runtime.core.context.TenantContext;
import java.util.List;

public interface WorkflowInstanceReader {
    List<WorkflowInstanceView> findActiveByTenant(TenantContext tenant);
}
