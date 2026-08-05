package io.devinebyte.runtime.workflow;

import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.module.ModuleIsolationGuard;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import io.devinebyte.runtime.workflow.engine.WorkflowInstanceRepository;
import io.devinebyte.runtime.workflow.engine.WorkflowExecutor;
import io.devinebyte.runtime.workflow.loader.WorkflowLoader;

public record RuntimeWorkflowModule(WorkflowLoader loader, WorkflowEngine engine) {
    public static RuntimeWorkflowModule create(EventStore store, ModuleIsolationGuard guard) {
        WorkflowInstanceRepository repo = new WorkflowInstanceRepository(store);
        WorkflowExecutor executor = new WorkflowExecutor(store, guard);
        WorkflowEngine engine = new WorkflowEngine(repo, executor);
        WorkflowLoader loader = new WorkflowLoader();
        return new RuntimeWorkflowModule(loader, engine);
    }
}
