package io.devinebyte.runtime.orchestration;

import io.devinebyte.runtime.orchestration.runtime.api.ContractRouteRegistry;
import io.devinebyte.runtime.orchestration.runtime.api.RuntimeApiServer;
import io.devinebyte.runtime.orchestration.runtime.scheduler.SchedulerEngine;
import io.devinebyte.runtime.orchestration.runtime.security.PermissionEngine;
import io.devinebyte.runtime.orchestration.runtime.security.SecurityRuntime;
import io.devinebyte.runtime.core.registry.RuntimeRegistry;
import io.devinebyte.runtime.event.core.EventBus;
import io.devinebyte.runtime.module.ModuleRegistry;
import io.devinebyte.runtime.workflow.engine.WorkflowEngine;
import jakarta.inject.Singleton;
import java.nio.file.FileSystem;

@Singleton
public record RuntimeOrchestrationModule(
    SecurityRuntime securityRuntime,
    PermissionEngine permissionEngine,
    ContractRouteRegistry routeRegistry,
    RuntimeApiServer apiServer,
    SchedulerEngine schedulerEngine
) {
    public static RuntimeOrchestrationModule create(
        EventBus eventBus,
        ModuleRegistry moduleRegistry,
        RuntimeRegistry runtimeRegistry,
        WorkflowEngine workflowEngine,
        FileSystem fs
    ) {
        var permissionEngine = new PermissionEngine();
        var routeRegistry = new ContractRouteRegistry(fs); 
        var securityRuntime = new SecurityRuntime(permissionEngine, moduleRegistry, routeRegistry); 
        var schedulerEngine = new SchedulerEngine(eventBus);
        var apiServer = new RuntimeApiServer(securityRuntime, routeRegistry, runtimeRegistry, workflowEngine);

        return new RuntimeOrchestrationModule(securityRuntime, permissionEngine, routeRegistry, apiServer, schedulerEngine);
    }
}
